package com.example.speech.control;

import com.example.speech.model.Message;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;

public class MessageCellCreator implements Callback<ListView<Message>, ListCell<Message>> {

    @Override
    public ListCell<Message> call(ListView<Message> listView) {
        return new ListCell<Message>() {
            private VBox container = new VBox();
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

                    // Настройка ширины
                    if (getListView() != null) {
                        double maxWidth = getListView().getWidth() - 100;
                        controller.setMaxWidth(maxWidth);
                    }

                    controller.initializeMessage(message);
                    return node;
                } catch (IOException e) {
                    e.printStackTrace();
                    return new javafx.scene.control.Label("Ошибка загрузки сообщения");
                }
            }
        };
    }
}