package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.service.ChannelUserService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageListener implements Runnable {
    private final String jdbcUrl;
    private final String dbUser;
    private final String userPassword;
    private final Consumer<Long> onNewMessage;

    private final List<ChannelUser> userChannels = new CopyOnWriteArrayList<>();

    private final Set<String> subscribedChannels = ConcurrentHashMap.newKeySet();

    private volatile Connection activeConnection;
    private volatile boolean running = true;
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);

    private static final int RECONNECT_DELAY_MS = 5000;  // 5 секунд между попытками
    private static final int NOTIFICATION_TIMEOUT_MS = 5000;

    private final SpeechBaseController speechBaseController;

    private final BlockingQueue<Runnable> commandQueue = new LinkedBlockingQueue<>();

    public MessageListener(String jdbcUrl, String dbUser, String userPassword,
                           Consumer<Long> onNewMessage, List<ChannelUser> initialChannels,
                           SpeechBaseController speechBaseController) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.userPassword = userPassword;
        this.onNewMessage = onNewMessage;
        if (initialChannels != null) {
            for (ChannelUser cu : initialChannels) {
                addChannelAsync(cu);
            }
        }
        this.speechBaseController = speechBaseController;
    }

    private void internalAddChannel(ChannelUser channelUser) {
        String channelName = "channel_" + channelUser.getChannel().getChannelID();
        if (subscribedChannels.add(channelName)) {
            userChannels.add(channelUser);
            if (activeConnection != null && !isConnectionClosed(activeConnection)) {
                try (Statement st = activeConnection.createStatement()) {
                    st.execute("LISTEN " + channelName);
                } catch (SQLException e) {
                    System.err.println("Не удалось подписаться на канал " + channelName + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Удалить канал из прослушивания (если нужно).
     */
    public boolean removeChannel(ChannelUser channelUser) {
        String channelName = "channel_" + channelUser.getChannel().getChannelID();
        if (subscribedChannels.remove(channelName)) {
            userChannels.remove(channelUser);
            Connection conn = activeConnection;
            if (conn != null && !isConnectionClosed(conn)) {
                try (Statement st = conn.createStatement()) {
                    st.execute("UNLISTEN " + channelName);
                } catch (SQLException e) {
                    System.err.println("Ошибка при отписке от канала " + channelName + ": " + e.getMessage());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Остановить слушатель (завершить поток).
     */
    public void stop() {
        running = false;
        // Прерываем ожидание getNotifications, закрывая соединение
        closeQuietly(activeConnection);
    }

    @Override
    public void run() {
        while (running) {
            try {
                connectAndListen();  // устанавливает соединение и слушает уведомления
            } catch (Exception e) {
                if (running) {
                    System.err.println("Критическая ошибка в слушателе: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (running && !Thread.currentThread().isInterrupted()) {
                // Пауза перед переподключением
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        closeQuietly(activeConnection);
    }

    private void connectAndListen() throws SQLException, InterruptedException {
        Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, userPassword);
        this.activeConnection = conn;

        // Подписываемся на все каналы из текущего списка
        try (Statement st = conn.createStatement()) {
            for (ChannelUser cu : userChannels) {
                String channelName = "channel_" + cu.getChannel().getChannelID();
                st.execute("LISTEN " + channelName);
            }

            st.execute("LISTEN new_channel_for_user_" + speechBaseController.getCurrentUser().getIdUser());
        }

        PGConnection pgConn = conn.unwrap(PGConnection.class);

        // Цикл получения уведомлений
        while (running && !Thread.currentThread().isInterrupted() && !isConnectionClosed(conn)) {
            List<Runnable> commands = new ArrayList<>();
            commandQueue.drainTo(commands);
            for (Runnable cmd : commands) cmd.run();

            PGNotification[] notifications = pgConn.getNotifications(NOTIFICATION_TIMEOUT_MS);
            if (notifications != null) {
                for (PGNotification n : notifications) {
                    String channelName = n.getName();
                    String payload = n.getParameter();

                    if (channelName.startsWith("channel_")) {
                        try {
                            long messageId = Long.parseLong(payload);
                            onNewMessage.accept(messageId);
                        } catch (NumberFormatException e) {
                            System.err.println("Неверный формат messageId: " + payload);
                        }
                    } else if (channelName.startsWith("new_channel_for_user_")) {
                        try {
                            long channelUserId = Long.parseLong(payload);
                            boolean isDelete = (channelUserId < 0);
                            long absId = Math.abs(channelUserId);

                            if (isDelete) {
                                // Удаление: ищем ChannelUser в локальном списке и убираем
                                ChannelUser toRemove = speechBaseController.userChats.stream()
                                        .filter(cu -> cu.getChannelUserId() == absId)
                                        .findFirst()
                                        .orElse(null);
                                if (toRemove != null) {
                                    removeChannel(toRemove);
                                    Platform.runLater(() -> {
                                        speechBaseController.userChats.remove(toRemove);
                                    });
                                }
                            } else {
                                // Добавление: загружаем свежий ChannelUser
                                ChannelUser newCu = new ChannelUserService().getRowById(channelUserId);
                                if (newCu != null && !speechBaseController.userChats.contains(newCu)) {
                                    addChannelAsync(newCu);
                                    Platform.runLater(() -> {
                                        speechBaseController.userChats.add(newCu);
                                    });
                                } else {
                                    Platform.runLater(() -> {
                                        ObservableList<ChannelUser> userChats = speechBaseController.userChats;
                                        userChats.set(userChats.indexOf(userChats.stream()
                                                .filter(uC -> uC.getChannelUserId() == channelUserId)
                                                        .findFirst().orElse(newCu)), newCu);
                                        speechBaseController.updateNameAndStatusChannel(newCu);
                                    });
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Неверный формат channelId: " + payload);
                        }
                    }
                }
            }
        }

        // Если вышли из цикла не по запросу остановки, значит соединение разорвано
        if (running && !Thread.currentThread().isInterrupted()) {
            System.err.println("Соединение разорвано, инициируем переподключение...");
            throw new SQLException("Connection lost");
        }
    }

    public void addChannelAsync(ChannelUser channelUser) {
        commandQueue.offer(() -> internalAddChannel(channelUser));
    }


    private boolean isConnectionClosed(Connection conn) {
        try {
            return conn == null || conn.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }
}