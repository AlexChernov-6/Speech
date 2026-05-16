package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageListener implements Runnable {
    private final String jdbcUrl;
    private final String dbUser;
    private final String userPassword;
    private final Consumer<Long> onNewMessage;

    // Потокобезопасный список каналов (добавление/удаление во время работы)
    private final List<ChannelUser> userChannels = new CopyOnWriteArrayList<>();

    // Для быстрого поиска по имени канала (чтобы избежать дублирования)
    private final Set<String> subscribedChannels = ConcurrentHashMap.newKeySet();

    private volatile Connection activeConnection;
    private volatile boolean running = true;
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);

    private static final int RECONNECT_DELAY_MS = 5000;  // 5 секунд между попытками
    private static final int NOTIFICATION_TIMEOUT_MS = 5000;

    public MessageListener(String jdbcUrl, String dbUser, String userPassword,
                           Consumer<Long> onNewMessage, List<ChannelUser> initialChannels) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.userPassword = userPassword;
        this.onNewMessage = onNewMessage;
        if (initialChannels != null) {
            for (ChannelUser cu : initialChannels) {
                addChannel(cu);
            }
        }
    }

    /**
     * Добавить новый канал для прослушивания (можно вызывать из любого потока).
     * @return true, если канал был добавлен (или уже был подписан)
     */
    public boolean addChannel(ChannelUser channelUser) {
        String channelName = "channel_" + channelUser.getChannel().getChannelID();
        if (subscribedChannels.add(channelName)) {
            userChannels.add(channelUser);
            // Если соединение активно, выполняем LISTEN прямо сейчас
            Connection conn = activeConnection;
            if (conn != null && !isConnectionClosed(conn)) {
                try (Statement st = conn.createStatement()) {
                    st.execute("LISTEN " + channelName);
                    System.out.println("Подписка на канал " + channelName + " добавлена динамически");
                    return true;
                } catch (SQLException e) {
                    System.err.println("Не удалось подписаться на канал " + channelName + ": " + e.getMessage());
                    // Если ошибка, канал уже добавлен в список, при переподключении он будет заново подписан
                    return false;
                }
            }
            return true;
        }
        return false; // уже подписан
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
                    System.out.println("Отписка от канала " + channelName);
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
                System.out.println("Подписка на канал " + channelName + " выполнена");
            }
        }

        PGConnection pgConn = conn.unwrap(PGConnection.class);

        // Цикл получения уведомлений
        while (running && !Thread.currentThread().isInterrupted() && !isConnectionClosed(conn)) {
            PGNotification[] notifications = pgConn.getNotifications(NOTIFICATION_TIMEOUT_MS);
            if (notifications != null) {
                for (PGNotification n : notifications) {
                    try {
                        long messageId = Long.parseLong(n.getParameter());
                        onNewMessage.accept(messageId);
                    } catch (NumberFormatException e) {
                        System.err.println("Неверный формат payload: " + n.getParameter());
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