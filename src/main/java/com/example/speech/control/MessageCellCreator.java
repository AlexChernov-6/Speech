package com.example.speech.control;

import com.example.speech.model.Message;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageCellCreator implements Callback<ListView<Message>, ListCell<Message>> {

    private SpeechBaseController speechBaseController;
    private Message nextMessage;
    private Map<Message, TextMessageCellController> controllerCache = new HashMap<>();

    public MessageCellCreator(SpeechBaseController speechBaseController) {
        this.speechBaseController = speechBaseController;
    }

    @Override
    public ListCell<Message> call(ListView<Message> listView) {
        return new ListCell<Message>() {
            private final VBox container = new VBox();
            private javafx.scene.Node dateNode = null;
            private javafx.scene.Node messageNode = null;

            {
                container.setSpacing(5);
            }

            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);

                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                    container.getChildren().clear();
                    dateNode = null;
                    messageNode = null;
                } else {
                    container.getChildren().clear();

                    boolean shouldShowDate = shouldShowDate(message, getIndex());

                    if (shouldShowDate) {
                        dateNode = createDateCell(message);
                        container.getChildren().add(dateNode);
                    }

                    messageNode = createTextCell(message);
                    container.getChildren().add(messageNode);

                    setGraphic(container);
                    setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                }
            }

            private boolean shouldShowDate(Message message, int index) {
                if (index == 0) return true;

                if (getListView() != null && getListView().getItems().size() > 1 && index > 0) {
                    Message prevMessage = getListView().getItems().get(index - 1);
                    return !message.getMessageDatetime().toLocalDate()
                            .equals(prevMessage.getMessageDatetime().toLocalDate());
                }
                return false;
            }

            private javafx.scene.Node createDateCell(Message message) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/com/example/speech/shape/DateMessageCellShape.fxml"));
                    javafx.scene.Node node = loader.load();
                    DateMessageCellController controller = loader.getController();

                    // Настройка ширины
                    if (getListView() != null) {
                        double maxWidth = getListView().getWidth() * 0.6;
                        controller.setMaxWidth(maxWidth);
                    }

                    String dateStr = message.getMessageDatetime().toLocalDate()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    controller.setDate(dateStr);

                    return node;
                } catch (IOException e) {
                    e.printStackTrace();
                    return new javafx.scene.control.Label("Ошибка загрузки даты");
                }
            }

            private javafx.scene.Node createTextCell(Message message) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/com/example/speech/shape/TextMessageCellShape.fxml"));
                    javafx.scene.Node node = loader.load();
                    TextMessageCellController controller = loader.getController();

                    controllerCache.put(message, controller);

                    // Настройка ширины
                    if (getListView() != null) {
                        double maxWidth = getListView().getWidth() - 100;
                        controller.setMaxWidthGP(maxWidth);
                    }

                    controller.initializeMessage(speechBaseController, message
                            , shouldShowAvatarForMessage(message, getIndex()));
                    return node;
                } catch (IOException e) {
                    e.printStackTrace();
                    return new javafx.scene.control.Label("Ошибка загрузки сообщения");
                }
            }

            private boolean shouldShowAvatarForMessage(Message currentMessage, int currentIndex) {
                ListView<Message> listView = getListView();
                if (listView == null) return true;

                int totalItems = listView.getItems().size();

                // 1. Если это последнее сообщение в списке - показываем аватар
                if (currentIndex == totalItems - 1) return true;

                // 2. Получаем следующее сообщение
                nextMessage = listView.getItems().get(currentIndex + 1);

                // 3. Если следующее сообщение от другого пользователя - показываем аватар
                if (!Objects.equals(nextMessage.getChannelUser().getUser().getIdUser(),
                        currentMessage.getChannelUser().getUser().getIdUser())) {
                    return true;
                }

                // 4. Если следующее сообщение отправлено через большой промежуток времени (> 15 мин) - показываем аватар
                long minutesBetween = java.time.Duration.between(
                        currentMessage.getMessageDatetime(),
                        nextMessage.getMessageDatetime()
                ).toMinutes();

                if (minutesBetween >= 15) {
                    return true;
                }

                // 5. Во всех остальных случаях не показываем аватар
                return false;
            }
        };
    }

    public TextMessageCellController getControllerCache(Message message) {
        return controllerCache.get(message);
    }
}