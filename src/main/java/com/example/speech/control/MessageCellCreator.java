package com.example.speech.control;

import com.example.speech.model.Message;
import com.example.speech.model.MessageContent;
import com.example.speech.util.FileUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class MessageCellCreator implements Callback<ListView<Message>, ListCell<Message>> {

    private final SpeechBaseController speechBaseController;
    private final Map<Message, TextMessageCellController> controllerCache = new HashMap<>();

    public MessageCellCreator(SpeechBaseController speechBaseController) {
        this.speechBaseController = speechBaseController;
    }

    @Override
    public ListCell<Message> call(ListView<Message> listView) {
        return new ListCell<Message>() {
            private final VBox container = new VBox();
            private Node dateNode = null;
            private Node messageNode = null;
            private Message currentMessage = null;

            {
                container.setSpacing(5);
            }

            @Override
            protected void updateItem(Message message, boolean empty) {
                if (currentMessage != null && empty) {
                    controllerCache.remove(currentMessage);
                    currentMessage = null;
                }
                // Удаляем старый контроллер из кэша, если сообщение изменилось
                if (currentMessage != null && !message.equals(currentMessage)) {
                    controllerCache.remove(currentMessage);
                }

                super.updateItem(message, empty);
                currentMessage = empty ? null : message;

                container.getChildren().clear();
                getStyleClass().add("list-cell-transparent");

                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                    dateNode = null;
                    messageNode = null;
                    return;
                }

                boolean shouldShowDate = shouldShowDate(message, getIndex());
                if (shouldShowDate) {
                    dateNode = createDateCell(message);
                    container.getChildren().add(dateNode);
                }

                messageNode = createTextCell(message);
                container.getChildren().add(messageNode);
                setGraphic(container);
            }

            private boolean shouldShowDate(Message message, int index) {
                if (index == 0) return true;
                ListView<Message> lv = getListView();
                if (lv != null && lv.getItems().size() > 1 && index > 0) {
                    Message prev = lv.getItems().get(index - 1);
                    return !message.getMessageDatetime().toLocalDate()
                            .equals(prev.getMessageDatetime().toLocalDate());
                }
                return false;
            }

            private Node createDateCell(Message message) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/com/example/speech/shape/DateMessageCellShape.fxml"));
                    Node node = loader.load();
                    DateMessageCellController controller = loader.getController();
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

            private Node createTextCell(Message message) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/com/example/speech/shape/TextMessageCellShape.fxml"));
                    Node node = loader.load();
                    TextMessageCellController controller = loader.getController();
                    controller.setMessagesListView(getListView());
                    controller.setSelectionModeActive(speechBaseController.isSelectionModeActive());
                    controllerCache.put(message, controller);
                    controller.initializeMessage(speechBaseController, message);
                    return node;
                } catch (IOException e) {
                    e.printStackTrace();
                    return new javafx.scene.control.Label("Ошибка загрузки сообщения");
                }
            }
        };
    }

    public TextMessageCellController getControllerCache(Message message) {
        return controllerCache.get(message);
    }

    public Map<Message, TextMessageCellController> getControllerCache() {
        return controllerCache;
    }

    public void clearCache() {
        controllerCache.clear();
    }
}