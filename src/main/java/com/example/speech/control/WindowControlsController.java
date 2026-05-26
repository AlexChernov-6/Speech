package com.example.speech.control;

import com.example.speech.model.Channel;
import com.example.speech.model.ChannelUser;
import com.example.speech.service.ChannelUserService;
import com.example.speech.service.UserService;
import com.example.speech.util.HelpfulClass;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

import static com.example.speech.control.EntranceController.CONFIG_MANAGER;
import static com.example.speech.control.SpeechBaseController.currentUser;

public class WindowControlsController {

    private boolean isFullScreen = false;
    private Stage stage;

    @FXML
    private Button wrapBtn, expandBtn, closeBtn;

    @FXML
    private void onWrapBtn() {
        stage.setIconified(true);
    }

    @FXML
    private void onExpandBtn() {
        stage.setFullScreen(!stage.isFullScreen());
        updateBackgroundExpandBtn();
    }

    @FXML
    private void onCloseBtn() {
        stage.hide();

        Thread updateStatusThread = new Thread(() -> {
            try {
                ChannelUserService channelUserService = new ChannelUserService();

                if(currentUser != null) {
                    currentUser.setStatusUser("не в сети");
                    new UserService().update(currentUser);

                    List<Channel> channels = channelUserService.getAllChatsByUser(currentUser).stream()
                            .map(ChannelUser::getChannel)
                            .filter(channel -> channel.getChannelType().getChannelTypeId() == 3)
                            .toList();
                    for (Channel channel : channels) {
                        ChannelUser channelUser = channelUserService
                                .getInterlocutorUserChannelInChannel(channel.getChannelID(), currentUser.getIdUser());
                        if (channelUser != null) {
                            channelUser.setStatusOfTheInterlocutor(currentUser.getStatusUser());
                            channelUserService.update(channelUser);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> stage.close());
            }
        });
        updateStatusThread.setDaemon(false);
        updateStatusThread.start();
    }

    @FXML
    private void initialize() {
        HelpfulClass.setImageWithButton(wrapBtn, "wrap-window.png", "window-control-button", 25, 40);
        HelpfulClass.setImageWithButton(closeBtn, "close-application.png", "window-control-button", 25, 40);
        closeBtn.getStyleClass().add("close-button");
        Platform.runLater(() -> {
            PauseTransition pauseTransition = new PauseTransition(Duration.millis(200));
            pauseTransition.setOnFinished(e -> {
                try {
                    stage = (Stage) closeBtn.getScene().getWindow();

                    isFullScreen = CONFIG_MANAGER.getIsFullScreen();
                    stage.fullScreenProperty().addListener((ob, oldV, newV) -> {
                        isFullScreen = newV;
                        CONFIG_MANAGER.setIsFullScreen(newV);
                        CONFIG_MANAGER.save();
                    });
                    updateBackgroundExpandBtn();

                    stage.widthProperty().addListener((ob, o, n) -> {
                        CONFIG_MANAGER.setWindowWidth(n.doubleValue());
                        CONFIG_MANAGER.save();
                    });

                    stage.heightProperty().addListener((ob, o, n) -> {
                        CONFIG_MANAGER.setWindowHeight(n.doubleValue());
                        CONFIG_MANAGER.save();
                    });

                    stage.xProperty().addListener((ob, o, n) -> {
                        CONFIG_MANAGER.setWindowX(n.doubleValue());
                        CONFIG_MANAGER.save();
                    });

                    stage.yProperty().addListener((ob, o, n) -> {
                        CONFIG_MANAGER.setWindowY(n.doubleValue());
                        CONFIG_MANAGER.save();
                    });
                } catch (NullPointerException ignore) {

                }
            });
            pauseTransition.play();
        });
    }

    private void updateBackgroundExpandBtn() {
        if (isFullScreen)
            HelpfulClass.setImageWithButton(expandBtn, "fit-screen.png", "window-control-button", 25, 40);
        else
            HelpfulClass.setImageWithButton(expandBtn, "full-screen-image.png", "window-control-button", 25, 40);
    }
}
