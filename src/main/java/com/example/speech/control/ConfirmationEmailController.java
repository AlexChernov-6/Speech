package com.example.speech.control;

import com.example.speech.model.User;
import com.example.speech.service.UserService;
import com.example.speech.util.HelpfulClass;
import com.example.speech.util.HelpfulInitializationClass;
import com.example.speech.util.SendingClass;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.speech.util.HelpfulValidationClass.validConfirmCode;
import static com.example.speech.util.NavigateListener.setEnterPressed;
import static com.example.speech.util.NavigateListener.setLinkListener;
import static com.example.speech.util.SendingClass.*;

public class ConfirmationEmailController {
    @FXML
    private Button closeBtn, getCodeBtn, confirmBtn;

    @FXML
    private Label contentLb, informationLb;

    @FXML
    private TextField num1, num2, num3, num4, num5, num6;

    private User newUser;

    private String mail;

    private Timer countdownTimer;

    private Stage mainStage;

    @FXML
    private void onCloseBtn() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    public void showModalConfirmationEmailStage(Stage mainStage, User newUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/ConfirmationEmailShape.fxml"));
        Parent parent = loader.load();

        ConfirmationEmailController controller = loader.getController();
        controller.initializeData(newUser, mainStage);

        HelpfulInitializationClass.showModalStage(mainStage, parent);
    }

    private void initializeData(User newUser, Stage mainStage) {
        this.mail = newUser.getEmailUser();
        this.mainStage = mainStage;
        this.newUser = newUser;
        initializeContentLb();
        setLinkListener(List.of(num1, num2, num3, num4, num5, num6), confirmBtn);
        Platform.runLater(() -> num1.requestFocus());
        setEnterPressed(confirmBtn);
        HelpfulClass.setImageWithButton(closeBtn, "close-application.png","window-control-button", 25, 25);
        closeBtn.getStyleClass().add("close-button");
        closeBtn.setStyle("-fx-background-radius: 8px;");
        Platform.runLater(this::onGetCodeBtn);
    }

    private void initializeContentLb() {
        if (contentLb != null && mail != null) {
            TextFlow textFlow = new TextFlow();

            Text before = new Text("Мы отправили код на адрес: ");
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

    @FXML
    private void onGetCodeBtn() {
        if (getCodeBtn.getText().equals("Получить код")) {
            // ПРОВЕРЯЕМ можно ли отправлять ДО отправки
            if (!SendingClass.canSendEmail(mail)) {
                int remaining = SendingClass.getRemainingTime(mail);
                startCountdown(remaining);
                return;
            }

            startCountdown(60);

            // ОТПРАВЛЯЕМ email в отдельном потоке, чтобы не блокировать UI
            Thread sentPostalDeliveryThread = new Thread(() -> {
                boolean sent = SendingClass.sendPostalDelivery(mail, ContextDelivery.SEND_CONFIRMATION_CODE);

                Platform.runLater(() -> {
                    if (sent) {
                        informationLb.setText("Код отправлен на вашу почту");
                        informationLb.setStyle("-fx-text-fill: green");
                    } else {
                        informationLb.setText("Ошибка отправки кода");
                        informationLb.setStyle("-fx-text-fill: red");
                    }
                });
            });
            sentPostalDeliveryThread.setDaemon(true);
            sentPostalDeliveryThread.start();
        }
    }

    private void startCountdown(int startSeconds) {
        // Отменяем предыдущий таймер
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }

        getCodeBtn.setDisable(true);
        final int[] secondsRemaining = {startSeconds};

        countdownTimer = new Timer();
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    secondsRemaining[0]--;
                    if (secondsRemaining[0] > 0) {
                        getCodeBtn.setText("Повторно через " + secondsRemaining[0]);
                    } else {
                        getCodeBtn.setText("Получить код");
                        getCodeBtn.setDisable(false);
                        countdownTimer.cancel();
                        countdownTimer = null;
                    }
                });
            }
        }, 1000, 1000);
    }

    @FXML
    private void onConfirmBtn() throws IOException {
        if (validConfirmCode(informationLb, num1, num2, num3, num4, num5, num6)) {
            if (getVerificationCode(mail) != null &&
                    getVerificationCode(mail).equals(getEnteredCode(num1, num2, num3, num4, num5, num6))) {
                newUser.setStatusUser("в сети");

                if (new UserService().save(newUser)) {
                    FXMLLoader fxmlLoader = new FXMLLoader(EntranceController.class.getResource(
                            "/com/example/speech/shape/SpeechBaseShape.fxml"
                    ));
                    Parent speechBaseRoot = fxmlLoader.load();

                    SpeechBaseController controller = fxmlLoader.getController();
                    controller.initializeData(mainStage, newUser);
                    //Меняем разметку окна авторизации на разметку основного окна
                    mainStage.getScene().setRoot(speechBaseRoot);
                    onCloseBtn();
                } else {
                    System.err.println("Неверные данные для регистрации");
                }
            } else {
                informationLb.setText("Неверный код");
                informationLb.setStyle("-fx-text-fill: rgba(115,0,0);");
            }
        }
    }

    public static String getEnteredCode(TextField... textFields) {
        StringBuilder sB = new StringBuilder();
        for (TextField tF : textFields) {
            sB.append(tF.getText());
        }
        return sB.toString();
    }
}
