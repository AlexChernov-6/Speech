package com.example.speech.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.time.LocalDate;

import static com.example.speech.util.HelpfulValidationClass.MONTHS;

public class HelpfulInitializationClass {
    //Метод, который будет устанавливать значения в ComboBox, в качестве аргумента принимает множество ComboBox
    //С неопределённым типом данных, обрабатывается проверка какие значение раздавать по id
    public static void setValuesComboBox(ComboBox<?>... comboBoxes) {
        for(ComboBox<?> comboBox : comboBoxes) {
            String comboId = comboBox.getId();

            switch (comboId) {
                case "dayBirthdayCB":
                    for (int i = 1; i <= 31; i++) {
                        //Максимум в месяце 31 день
                        ((ComboBox<Integer>) comboBox).getItems().add(i);
                    }
                    break;
                case "monthBirthdayCB":
                    ((ComboBox<String>) comboBox).getItems().addAll(MONTHS);
                    break;

                case "yearBirthdayCB":
                    //Берём последние 100 лет цикл будет не инкрементировать, а дикрементировать
                    int currentYear = LocalDate.now().getYear();
                    for (int i = currentYear - 18; i >= currentYear - 100; i--) {
                        ((ComboBox<Integer>) comboBox).getItems().add(i);
                    }
            }
        }
    }

    public static Stage showModalStage(Stage mainStage, Parent modalStageParent) {
        //Создаём окно, которому назначим владельца(mainStage) и укажем что окно модальное
        //Тоесть пока открыто данное окно, взаимодействие с другими окнами запрещено
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.WINDOW_MODAL);
        modalStage.initOwner(mainStage);
        modalStage.initStyle(StageStyle.TRANSPARENT);

        // Затемнение
        applyDimmingEffect(true, mainStage);

        //Создаём сцену, которая будет прозрачной и задаём размеры 1/2 длинны окна-владельца и 1/2 его высоты
        Scene scene = new Scene(modalStageParent, 400 , 250);
        scene.setFill(Color.TRANSPARENT);
        modalStage.setScene(scene);

        //Задаём стартовое положение окна
        modalStage.setX(mainStage.getX() + (mainStage.getWidth() - 400) / 2);
        modalStage.setY(mainStage.getY() + (mainStage.getHeight() - 250) / 2);

        // Слушатель закрытия для убирания затемнения
        modalStage.setOnHidden(e -> applyDimmingEffect(false, mainStage));

        modalStage.show();
        return modalStage;
    }

    //Метод, который затемняет окно владельца
    public static void applyDimmingEffect(boolean dim, Stage mainStage) {
        if (mainStage != null && mainStage.getScene() != null) {
            //Получаем корневой Pane
            Pane rootPane = (Pane) mainStage.getScene().getRoot();

            if (rootPane != null) {
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
                    rootPane.getChildren().add(overlay);
                } else {
                    //Удаляем Region, по id, который мы задали выше
                    rootPane.getChildren().removeIf(node ->
                            node instanceof Region && "global-dim-overlay".equals(node.getId()));
                }
            }
        }
    }
}
