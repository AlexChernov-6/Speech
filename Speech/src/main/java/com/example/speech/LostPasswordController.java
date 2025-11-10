package com.example.speech;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class LostPasswordController {

    @FXML
    private Label contentLb;

    private Stage stage;
    private Stage mainStage;
    private String mail;

    public static void showLostPasswordModal(String mail, Stage mainStage) throws IOException {
        //Создаём окно, которому назначим владельца(mainStage) и укажем что окно модальное
        //Тоесть пока открыто данное окно, взаимодействие с другими окнами запрещено
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.WINDOW_MODAL);
        modalStage.initOwner(mainStage);
        modalStage.initStyle(StageStyle.TRANSPARENT);
        //Задаём стартовое положение окна
        modalStage.setX(mainStage.getX() + mainStage.getWidth() / 6);
        modalStage.setY(mainStage.getY() + mainStage.getHeight() / 3);

        // Загружаем FXML и получаем контроллер
        FXMLLoader loader = new FXMLLoader(LostPasswordController.class.getResource("LostPasswordShape.fxml"));
        Parent root = loader.load();

        // Получаем контроллер созданный FXML
        LostPasswordController controller = loader.getController();

        // Инициализируем данные
        controller.mail = mail;
        controller.mainStage = mainStage;
        controller.stage = modalStage;

        // Обновляем текст
        controller.updateContentLabel();

        // Затемнение
        controller.applyDimmingEffect(true);

        //Создаём сцену, которая будет прозрачной и задаём размеры 2/3 длинны окна-владельца и 1/3 его высоты
        Scene scene = new Scene(root, mainStage.getWidth() * 2 / 3 , mainStage.getHeight() / 3);
        scene.setFill(Color.TRANSPARENT);
        modalStage.setScene(scene);

        // Слушатель закрытия для убирания затемнения
        modalStage.setOnHidden(e -> controller.applyDimmingEffect(false));

        modalStage.show();
    }

    //Метод, который обновляет текст у Label contentLb
    private void updateContentLabel() {
        if (contentLb != null && mail != null) {
            TextFlow textFlow = new TextFlow();

            Text before = new Text("Мы отправили инструкции по смене пароля на ");
            Text boldEmail = new Text(mail);
            Text after = new Text(", пожалуйста, проверьте папки «Входящие» и «Спам»");

            // Жирный email
            boldEmail.setStyle("-fx-font-weight: bold;");

            // Сохраняем стиль метки для обычного текста
            String labelStyle = contentLb.getStyle();
            before.setStyle(labelStyle);
            after.setStyle(labelStyle);

            textFlow.getChildren().addAll(before, boldEmail, after);

            contentLb.setGraphic(textFlow);
        }
    }

    //Метод, который затемняет окно владельца
    private void applyDimmingEffect(boolean dim) {
        if (mainStage != null && mainStage.getScene() != null) {
            //Получаем корневой AnchorPane
            AnchorPane rootAnchorPane = (AnchorPane) mainStage.getScene().getRoot();

            if (rootAnchorPane != null) {
                //Если в качестве входного параметра true, затемняем окно
                if (dim) {
                    //Создаем затемняющий Pane поверх всего, Region-прямоугольная зона, родительский класс для всех Pane
                    //Выбор Region обусловлен тем, что у него нету лишней логики позиционирования дочерних элементов
                    //Сам Region весит меньше чем остальные элементы Pane, что лучше сказывается на производительности
                    Region overlay = new Region();
                    //Задаём Region черный цвет с прозрачностью 0.6 и даём ему id
                    overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
                    overlay.setId("global-dim-overlay");

                    //Растягиваем на все окно
                    AnchorPane.setTopAnchor(overlay, 0.0);
                    AnchorPane.setBottomAnchor(overlay, 0.0);
                    AnchorPane.setLeftAnchor(overlay, 0.0);
                    AnchorPane.setRightAnchor(overlay, 0.0);

                    //Добавляем в корневой контейнер, так-как он добавлялся последним из всех элементов Parent
                    //То окажется поверх всех элементов и создаст эффект затемнения всех элементов Anchor
                    //За счёт прозрачности, если нужно больше затемнить элементы, то нужно уменьшить прозрачность
                    rootAnchorPane.getChildren().add(overlay);
                } else {
                    //Удаляем Region, по id, который мы задали выше
                    rootAnchorPane.getChildren().removeIf(node ->
                            node instanceof Region && "global-dim-overlay".equals(node.getId()));
                }
            }
        }
    }

    //События для кнопки OK
    @FXML
    private void onOkBtn() {
        applyDimmingEffect(false);
        if (stage != null) {
            stage.close();
        }
    }
}