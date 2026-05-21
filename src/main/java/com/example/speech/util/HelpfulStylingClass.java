package com.example.speech.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HelpfulStylingClass {
    public static void setStyleSheets(Parent... parents) {
        String cssUrl = HelpfulClass.class.getResource("/com/example/speech/styles.css").toExternalForm();
        for (Parent parent : parents) {
            parent.getStylesheets().add(cssUrl);
        }
    }

    public static void setRedEndChar(Label... labels) {
        for(Label label : labels) {
            String text = label.getText();
            if (text == null || text.isEmpty()) return;

            int labelLength = text.length();
            double percent = ((double) (labelLength - 1) / labelLength) * 100;
            label.setStyle("-fx-text-fill: linear-gradient(from 0% 0% to 100% 0%, " +
                    "#718096 0%, #718096 " + percent + "%, red " + percent + "%, red 100%);");
        }
    }

    //Метод, который следит за изменениями состояний окна, а конкретно за FullScreen
    public static void setupFullScreenListener(Stage stage, Pane rootPane) {
        stage.fullScreenProperty().addListener(observable -> {
            if (stage.isFullScreen())
                rootPane.setPadding(new Insets(0));
            else
                rootPane.setPadding(new Insets(13));
        });

        if (stage.isFullScreen())
            rootPane.setPadding(new Insets(0));
        else
            rootPane.setPadding(new Insets(13));
    }

    public static void applyPromptWithTF(TextInputControl textField) {
        String promptText = textField.getPromptText();

        textField.focusedProperty().addListener((ob, oldV, newV) -> {
            if(newV) {
                if (promptText != null && !promptText.isEmpty()) {
                    if (textField.getText().isEmpty()) {
                        textField.setText(textField.getPromptText());
                    } else {
                        if (textField.getText().contains(promptText))
                            textField.setText(textField.getText().replace(promptText, ""));
                    }
                }
            }
        });

        textField.setTextFormatter(new TextFormatter<>(change -> {
            String changeText = change.getText();
            String changeControlNewText = change.getControlNewText();
            String oldText = change.getControlText();

            if(changeControlNewText.equals(promptText) || changeControlNewText.isEmpty()) {
                textField.setStyle("-fx-text-fill: rgba(180,180,180);");
                if(changeControlNewText.isEmpty())
                    change.setText(promptText);
                change.setCaretPosition(0);
                change.setAnchor(0);
                return change;
            }

            if(!changeText.isEmpty() && oldText.equals(promptText)) {
                textField.setStyle("");
                change.setRange(0, oldText.length());
                change.setText(changeText);
                return change;
            }

            return change;
        }));
    }

    public static void scrollPaneAnimation(Node node) {
        Platform.runLater(() -> {
            ScrollBar vBar = (ScrollBar) node.lookup(".scroll-bar:vertical");
            if(vBar != null) {
                vBar.setStyle("-fx-pref-width: 10;");
                vBar.setOpacity(0.0);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(70), vBar);
                fadeIn.setToValue(1.0);

                FadeTransition fadeOut = new FadeTransition(Duration.millis(600), vBar);
                fadeOut.setToValue(0.0);

                PauseTransition hideTimer = new PauseTransition(Duration.seconds(2));
                hideTimer.setOnFinished(event -> {
                    fadeIn.stop();
                    fadeOut.playFromStart();
                });

                Runnable showBar = () -> {
                    fadeOut.stop();
                    if (vBar.getOpacity() < 1.0) {
                        fadeIn.playFromStart();
                    }
                    hideTimer.stop();
                    hideTimer.playFromStart();
                };

                vBar.setOnMouseEntered(e -> showBar.run());

                vBar.setOnMouseExited(e -> {
                    hideTimer.stop();
                    hideTimer.playFromStart();
                });

                vBar.valueProperty().addListener((ob, oldV, newV) -> {
                    if (oldV.doubleValue() != newV.doubleValue()) {
                        showBar.run();
                    }
                });
            }
        });
    }

    public static void scrollPaneAnimation(Node node, boolean isHorizontal) {
        Platform.runLater(() -> {
            ScrollBar vBar = (ScrollBar) node.lookup(".scroll-bar:horizontal");
            if(vBar != null) {
                vBar.setStyle("-fx-pref-width: 10;");
                vBar.setOpacity(0.0);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(70), vBar);
                fadeIn.setToValue(1.0);

                FadeTransition fadeOut = new FadeTransition(Duration.millis(600), vBar);
                fadeOut.setToValue(0.0);

                PauseTransition hideTimer = new PauseTransition(Duration.seconds(2));
                hideTimer.setOnFinished(event -> {
                    fadeIn.stop();
                    fadeOut.playFromStart();
                });

                Runnable showBar = () -> {
                    fadeOut.stop();
                    if (vBar.getOpacity() < 1.0) {
                        fadeIn.playFromStart();
                    }
                    hideTimer.stop();
                    hideTimer.playFromStart();
                };

                vBar.setOnMouseEntered(e -> showBar.run());

                vBar.setOnMouseExited(e -> {
                    hideTimer.stop();
                    hideTimer.playFromStart();
                });

                vBar.valueProperty().addListener((ob, oldV, newV) -> {
                    if (oldV.doubleValue() != newV.doubleValue()) {
                        showBar.run();
                    }
                });
            }
        });
    }
}
