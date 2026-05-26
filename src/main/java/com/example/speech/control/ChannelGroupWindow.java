package com.example.speech.control;

import com.example.speech.model.Channel;
import com.example.speech.model.ChannelUser;
import com.example.speech.model.Message;
import com.example.speech.model.User;
import com.example.speech.service.*;
import com.example.speech.util.HelpfulStylingClass;
import com.example.speech.util.ImageUtils;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.example.speech.util.HelpfulValidationClass.validateChannelNameShort;

public class ChannelGroupWindow extends StackPane {
    private final Pane shadowPane;
    private final StackPane parentStackPane;

    private final User currentUser;

    private HBox bottomHB, buttonsHB;

    private TextField channelNameTF, channelNameUniqueTF;
    private ImageView logoChannel;

    private final List<Label> hintLabels = new ArrayList<>();

    private final ChannelService channelService = new ChannelService();

    private final ChannelUserService channelUserService = new ChannelUserService();

    private boolean isOwner = false;

    private Channel currentChannel;

    private final SpeechBaseController speechBaseController;

    private ListView<User> userListView;
    private ListView<User> allUserListView;
    private ObservableList<User> userObservableList;
    private ObservableList<User> allUserObservableList;
    private FilteredList<User> allUserFilteredList;

    private double width = 450.0;

    private final ObjectProperty<Boolean> isUpdatingMode = new SimpleObjectProperty<>();
    private final ObjectProperty<Boolean> isAddedMode = new SimpleObjectProperty<>();

    private byte[] newChannelLogo;
    private String newChannelName, newChannelNameUnique;

    private StackPane logoSP;

    private Button choseLogoBtn;

    private VBox startVB;

    private Button cancelBtn;

    private TextField searchUserTF;

    private HBox bottomAddUserHB;

    public ChannelGroupWindow(SpeechBaseController speechBaseController) {
        this.speechBaseController = speechBaseController;
        this.parentStackPane = speechBaseController.getMessagesSP();
        this.currentUser = speechBaseController.getCurrentUser();
        isUpdatingMode.setValue(false);
        isAddedMode.setValue(false);

        setOpacity(0.0);
        setManaged(false);
        setMouseTransparent(true);
        setMaxWidth(width);
        setMaxHeight(550);
        getStyleClass().add("profile-vbox");

        shadowPane = new Pane();
        shadowPane.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        shadowPane.setMouseTransparent(false);
        shadowPane.setVisible(false);
        shadowPane.setManaged(false);
        parentStackPane.getChildren().add(shadowPane);

        createStartVB();

        isUpdatingMode.addListener((ob, oldV, newV) -> {
            if (newV && isOwner) {
                isAddedMode.setValue(false);

                if (cancelBtn != null) {
                    cancelBtn.setVisible(true);
                    cancelBtn.setManaged(true);
                }

                logoSP.setOnMouseEntered(e -> {
                    choseLogoBtn.setVisible(true);
                    choseLogoBtn.setManaged(true);
                });

                logoSP.setOnMouseExited(e -> {
                    choseLogoBtn.setVisible(false);
                    choseLogoBtn.setManaged(false);
                });

                userListView.setVisible(false);
                userListView.setManaged(false);

                if (channelNameTF == null) {
                    channelNameTF = createTextFields("НАЗВАНИЕ ГРУППЫ", currentChannel.getChannelName(), startVB, true);
                    channelNameTF.textProperty().addListener((ob1, oldV1, newV1) -> {
                        if (newV1.isEmpty() || newV1.equals("НАЗВАНИЕ ГРУППЫ")) {
                            if (channelNameTF.getUserData() != null && channelNameTF.getUserData() instanceof Label)
                                ((Label) channelNameTF.getUserData()).setText("Поле не может быть пустым");
                        } else {
                            if (channelNameTF.getUserData() != null && channelNameTF.getUserData() instanceof Label)
                                ((Label) channelNameTF.getUserData()).setText("НАЗВАНИЕ ГРУППЫ");
                            newChannelName = newV1;
                        }
                    });
                } else {
                    channelNameTF.getParent().setVisible(true);
                    channelNameTF.getParent().setManaged(true);
                    channelNameTF.setText(currentChannel.getChannelName());
                }

                if (channelNameUniqueTF == null) {
                    channelNameUniqueTF = createTextFields("ИМЯ ГРУППЫ(уникальное)"
                            , currentChannel.getChannel_name_unique().replace("@", ""), startVB, true);
                    channelNameUniqueTF.textProperty().addListener((ob1, oldV1, newV1) -> {
                        if (newV1.isEmpty() || newV1.equals("ИМЯ ГРУППЫ(уникальное)")) {
                            if (channelNameUniqueTF.getUserData() != null && channelNameUniqueTF.getUserData() instanceof Label)
                                ((Label) channelNameUniqueTF.getUserData()).setText("Поле не может быть пустым");
                        } else {
                            if (channelNameUniqueTF.getUserData() != null && channelNameUniqueTF.getUserData() instanceof Label) {
                                String valid = validateChannelNameShort(newV1);
                                if (valid == null) {
                                    ((Label) channelNameUniqueTF.getUserData()).setText("ИМЯ ГРУППЫ(уникальное)");
                                    newChannelNameUnique = newV1;
                                } else ((Label) channelNameUniqueTF.getUserData()).setText(valid);
                            }
                        }
                    });
                } else {
                    channelNameUniqueTF.getParent().setVisible(true);
                    channelNameUniqueTF.getParent().setManaged(true);
                    channelNameUniqueTF.setText(currentChannel.getChannel_name_unique().replace("@", ""));
                }

                if (bottomHB == null)
                    createBottomHB(startVB);
                else if (!Arrays.equals(newChannelLogo, currentChannel.getChannelLogo())) {
                    bottomHB.setVisible(true);
                    bottomHB.setManaged(true);
                }
            } else {
                logoSP.setOnMouseEntered(null);
                logoSP.setOnMouseExited(null);

                userListView.setVisible(true);
                userListView.setManaged(true);

                if (channelNameTF != null) {
                    channelNameTF.getParent().setVisible(false);
                    channelNameTF.getParent().setManaged(false);
                }

                if (channelNameUniqueTF != null) {
                    channelNameUniqueTF.getParent().setVisible(false);
                    channelNameUniqueTF.getParent().setManaged(false);
                }

                if (bottomHB != null) {
                    bottomHB.setVisible(false);
                    bottomHB.setManaged(false);
                }
            }
        });

        isAddedMode.addListener((ob, oldV, newV) -> {
            if (newV && isOwner) {
                isUpdatingMode.setValue(false);

                if (cancelBtn != null) {
                    cancelBtn.setVisible(true);
                    cancelBtn.setManaged(true);
                }

                userListView.setVisible(false);
                userListView.setManaged(false);

                if (searchUserTF == null) {
                    searchUserTF = createTextFields("Поиск по именам", null, startVB, false);
                    searchUserTF.textProperty().addListener((ob1, oldV1, newV1) -> {
                        if (!newV1.equals("Поиск по именам"))
                            allUserFilteredList.setPredicate(u -> u.getNameUser().toLowerCase().contains(newV1.toLowerCase()));
                        else
                            allUserFilteredList.setPredicate(u -> true);
                    });
                } else {
                    searchUserTF.getParent().setVisible(true);
                    searchUserTF.getParent().setManaged(true);
                }

                if (allUserListView == null) {
                    allUserListView = new ListView<>();
                    allUserListView.setVisible(false);
                    allUserListView.setManaged(false);
                    allUserListView.setCellFactory(e -> new UserCell());
                    VBox.setMargin(allUserListView, new Insets(10, 10, 10, 10));
                    Platform.runLater(() -> HelpfulStylingClass.scrollPaneAnimation(allUserListView));
                    VBox.setVgrow(allUserListView, Priority.ALWAYS);
                    allUserObservableList = FXCollections.observableArrayList(
                            new UserService().getAllRow().stream()
                                    .filter(u -> !u.equals(currentUser)).toList());
                    allUserFilteredList = new FilteredList<>(allUserObservableList, u -> true);
                    allUserListView.setItems(allUserFilteredList);
                    allUserListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                    allUserListView.getSelectionModel().selectedItemProperty()
                            .addListener((observable, oldVal, newVal) -> {
                                if (newVal != null) {
                                    bottomAddUserHB.setVisible(true);
                                    bottomAddUserHB.setManaged(true);
                                }
                            });
                    startVB.getChildren().add(allUserListView);
                }

                allUserListView.setVisible(true);
                allUserListView.setManaged(true);

                if (bottomAddUserHB == null) {
                    bottomAddUserHB = new HBox(10);
                    bottomAddUserHB.setAlignment(Pos.CENTER);
                    bottomAddUserHB.setPadding(new Insets(10, 10, 10, 10));
                    bottomAddUserHB.setVisible(false);
                    bottomAddUserHB.setManaged(false);

                    Button sendBtn = new Button("Отправить приглашение");
                    sendBtn.getStyleClass().add("login-button");
                    bottomAddUserHB.getChildren().add(sendBtn);
                    sendBtn.setOnAction(e -> {
                        List<User> usersInvited = new ArrayList<>(allUserListView.getSelectionModel().getSelectedItems());
                        allUserListView.getSelectionModel().clearSelection();

                        Thread sendInvitations = new Thread(() -> {
                            for (User user : usersInvited) {
                                Channel channel = channelService.getChannelByTwoUser(currentUser, user);

                                if (channel == null) {
                                    channel = new Channel();
                                    channel.setChannelType(new ChannelTypeService().getRowById(3L));
                                    channel.setChannel_name_unique(speechBaseController.getCurrentUser().getNameUser() + "_" + user.getNameUser());
                                    channel.setDisable_sharing(false);
                                    channelService.save(channel);

                                    ChannelUser channelUser1 = new ChannelUser();
                                    channelUser1.setUser(user);
                                    channelUser1.setChannel(channel);
                                    channelUser1.setVisibleNameChat(currentUser.getVisibleNameUser());
                                    channelUser1.setVisibleLogoChat(currentUser.getPhotoUser());
                                    channelUserService.save(channelUser1);

                                    ChannelUser channelUser2 = new ChannelUser();
                                    channelUser2.setUser(currentUser);
                                    channelUser2.setChannel(channel);
                                    channelUser2.setVisibleNameChat(user.getVisibleNameUser());
                                    channelUser2.setVisibleLogoChat(user.getPhotoUser());
                                    channelUserService.save(channelUser2);

                                    Message messageInvitation = new Message();
                                    messageInvitation.setMessageDatetime(LocalDateTime.now());
                                    messageInvitation.setChannelUser(channelUser2);
                                    messageInvitation.setChannelInvitations(currentChannel);
                                    boolean save = speechBaseController.getMessageService().save(messageInvitation);

                                    if(save) {
                                        messageInvitation.setMessageStatus("отправлено");
                                        speechBaseController.getMessageService().update(messageInvitation);
                                    } else
                                        messageInvitation.setMessageStatus("ошибка отправки");
                                } else {
                                    ChannelUser channelUser1 = channelUserService
                                            .getChannelUserByUserIdAndChannelId(user.getIdUser(), channel.getChannelID());

                                    if (channelUser1 == null) {
                                        channelUser1 = new ChannelUser();
                                        channelUser1.setUser(user);
                                        channelUser1.setChannel(channel);
                                        channelUser1.setVisibleNameChat(currentUser.getVisibleNameUser());
                                        channelUser1.setVisibleLogoChat(currentUser.getPhotoUser());
                                        channelUserService.save(channelUser1);
                                    }

                                    ChannelUser channelUser2 = channelUserService
                                            .getChannelUserByUserIdAndChannelId(currentUser.getIdUser(), channel.getChannelID());

                                    if (channelUser2 == null) {
                                        channelUser2 = new ChannelUser();
                                        channelUser2.setUser(currentUser);
                                        channelUser2.setChannel(channel);
                                        channelUser2.setVisibleNameChat(user.getVisibleNameUser());
                                        channelUser2.setVisibleLogoChat(user.getPhotoUser());
                                        channelUserService.save(channelUser2);
                                    }

                                    Message messageInvitation = new Message();
                                    messageInvitation.setChannelUser(channelUser2);
                                    messageInvitation.setChannelInvitations(currentChannel);
                                    boolean save = speechBaseController.getMessageService().save(messageInvitation);
                                    if(save) {
                                        messageInvitation.setMessageStatus("отправлено");
                                        speechBaseController.getMessageService().update(messageInvitation);
                                    } else
                                        messageInvitation.setMessageStatus("ошибка отправки");
                                }
                            }
                        });
                        sendInvitations.setDaemon(true);
                        sendInvitations.start();
                    });

                    Button canBtn = new Button("Отмена");
                    canBtn.getStyleClass().add("login-button");
                    bottomAddUserHB.getChildren().add(canBtn);
                    canBtn.setOnAction(e -> {
                        allUserListView.getSelectionModel().clearSelection();
                        bottomAddUserHB.setVisible(false);
                        bottomAddUserHB.setManaged(false);
                    });

                    startVB.getChildren().add(bottomAddUserHB);
                }
                if (!allUserListView.getSelectionModel().getSelectedItems().isEmpty()) {
                    bottomAddUserHB.setVisible(true);
                    bottomAddUserHB.setManaged(true);
                }
            } else {
                searchUserTF.getParent().setVisible(false);
                searchUserTF.getParent().setManaged(false);

                allUserListView.setVisible(false);
                allUserListView.setManaged(false);

                bottomAddUserHB.setVisible(false);
                bottomAddUserHB.setManaged(false);

                userListView.setVisible(true);
                userListView.setManaged(true);
            }
        });

        shadowPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (!isVisible()) return;

            Point2D point2D = screenToLocal(e.getScreenX(), e.getScreenY());
            if (point2D != null && contains(point2D)) return;
            else if (getOpacity() == 1.0) hideChannelGroupWidow();
        });
        parentStackPane.getChildren().add(this);
    }

    public void showChannelGroupWidow(List<User> userList) {
        userObservableList.setAll(userList);
        currentChannel = speechBaseController.getSelectedChannelUser().getChannel();
        if (currentUser.equals(currentChannel.getOwnerUser()))
            isOwner = true;

        if (isOwner) {
            buttonsHB.setManaged(true);
            buttonsHB.setVisible(true);
        } else {
            buttonsHB.setManaged(false);
            buttonsHB.setVisible(false);
        }

        logoChannel.setImage(currentChannel.getPhotoImage());

        if (channelNameTF != null)
            channelNameTF.setText(currentChannel.getChannelName());

        if (channelNameUniqueTF != null)
            channelNameUniqueTF.setText(currentChannel.getChannel_name_unique().replace("@", ""));

        newChannelLogo = currentChannel.getChannelLogo();
        newChannelName = currentChannel.getChannelName();
        newChannelNameUnique = currentChannel.getChannel_name_unique().replace("@", "");

        userListView.setCellFactory(e -> new UserCell(currentChannel.getOwnerUser(), speechBaseController));
        setManaged(true);
        setMouseTransparent(false);
        shadowPane.setVisible(true);
        shadowPane.setManaged(true);
        for (Node node : getChildren()) {
            if (node.getUserData() != null && node.getUserData().equals("bottomHB")) {
                node.setVisible(false);
                node.setManaged(false);
            } else {
                node.setVisible(true);
                node.setManaged(true);
            }
        }
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), this);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public void hideChannelGroupWidow() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            setManaged(false);
            setMouseTransparent(true);
            shadowPane.setVisible(false);
            shadowPane.setManaged(false);
            for (Node node : getChildren()) {
                node.setVisible(false);
                node.setManaged(false);
            }
        });
        fadeOut.play();
    }

    private void createStartVB() {
        startVB = new VBox();
        startVB.setManaged(false);
        startVB.setVisible(false);
        startVB.toFront();
        startVB.setMaxHeight(550);

        createButtonLogo(startVB);

        buttonsHB = new HBox(5);
        buttonsHB.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonsHB, new Insets(10, 10, 10, 10));
        startVB.getChildren().add(buttonsHB);

        Button addUserInGroupBtn = new Button("Добавить");
        addUserInGroupBtn.getStyleClass().add("button-chose-logo");
        buttonsHB.getChildren().add(addUserInGroupBtn);
        addUserInGroupBtn.setOnAction(e -> {
            isAddedMode.setValue(true);
        });

        Button updateGroupBtn = new Button("Изменить");
        updateGroupBtn.getStyleClass().add("button-chose-logo");
        buttonsHB.getChildren().add(updateGroupBtn);
        updateGroupBtn.setOnAction(e -> {
            isUpdatingMode.setValue(true);
        });

        Button deleteGroupBtn = new Button("Удалить");
        deleteGroupBtn.getStyleClass().add("button-chose-logo");
        buttonsHB.getChildren().add(deleteGroupBtn);
        deleteGroupBtn.setOnAction(e -> {
            new ConfirmationOfMessageDeletion().initializeShapeDeletionGroup(speechBaseController, currentChannel, this);
        });

        cancelBtn = new Button("Отмена");
        cancelBtn.getStyleClass().add("button-chose-logo");
        cancelBtn.setVisible(false);
        cancelBtn.setManaged(false);
        buttonsHB.getChildren().add(cancelBtn);
        cancelBtn.setOnAction(e -> {
            if (isUpdatingMode.get())
                isUpdatingMode.setValue(false);

            if (isAddedMode.get())
                isAddedMode.setValue(false);

            cancelBtn.setVisible(false);
            cancelBtn.setManaged(false);
        });

        userListView = new ListView<>();
        VBox.setMargin(userListView, new Insets(10, 10, 10, 10));
        Platform.runLater(() -> HelpfulStylingClass.scrollPaneAnimation(userListView));
        VBox.setVgrow(userListView, Priority.ALWAYS);
        userObservableList = FXCollections.observableArrayList();
        userListView.setItems(userObservableList);
        userListView.getSelectionModel().selectedItemProperty().addListener((ob, oldV, newV) -> {
            if(currentUser.equals(newV)) {
                if(speechBaseController.getProfileWindow() != null)
                    speechBaseController.getProfileWindow().showProfileWidow();
                else {
                    speechBaseController.setProfileWindow(new ProfileWindow(speechBaseController));
                    speechBaseController.getProfileWindow().showProfileWidow();
                }
            } else {
                if(speechBaseController.getOtherProfileWindow() != null)
                    speechBaseController.getOtherProfileWindow().showOtherProfileWindow(newV);
                else {
                    speechBaseController.setOtherProfileWindow(new OtherProfileWindow(speechBaseController));
                    speechBaseController.getOtherProfileWindow().showOtherProfileWindow(newV);
                }
            }
            Platform.runLater(() -> {
                userListView.getSelectionModel().clearSelection(userObservableList.indexOf(newV));
            });
        });
        startVB.getChildren().add(userListView);

        getChildren().add(startVB);
    }

    private void createButtonLogo(Pane parent) {
        logoSP = new StackPane();
        logoSP.setMaxWidth(width);
        logoSP.setMaxHeight(200);
        logoSP.setStyle("-fx-background-radius: 12 12 0 0;");

        Button logoBtn = new Button();
        logoBtn.getStyleClass().add("button-logo");
        logoBtn.setPrefHeight(200);
        logoBtn.setPrefWidth(width);
        logoBtn.setAlignment(Pos.CENTER);
        logoBtn.setOnAction(e -> {
            if (currentChannel.getChannelLogo() != null && currentChannel.getChannelLogo().length > 1)
                ImageUtils.viewingImages(parentStackPane, List.of(currentChannel.getPhotoImage()));
        });

        logoChannel = new ImageView();
        logoChannel.setFitWidth(width);
        logoChannel.setFitHeight(200);
        logoChannel.setPreserveRatio(true);
        logoChannel.setSmooth(true);

        logoBtn.setGraphic(logoChannel);

        logoSP.getChildren().add(logoBtn);

        choseLogoBtn = new Button("Выбрать фото");
        choseLogoBtn.getStyleClass().add("button-chose-logo");
        StackPane.setAlignment(choseLogoBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(choseLogoBtn, new Insets(0, 10, 10, 0));
        choseLogoBtn.setVisible(false);
        choseLogoBtn.setManaged(false);
        logoSP.getChildren().add(choseLogoBtn);
        choseLogoBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выбор иконки чата");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg"));

            File chosenFile = fileChooser.showOpenDialog(choseLogoBtn.getScene().getWindow());

            if (chosenFile == null) return;

            try {
                newChannelLogo = Files.readAllBytes(chosenFile.toPath());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            try {
                logoChannel.setImage(new Image(new FileInputStream(chosenFile)));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            bottomHB.setVisible(true);
            bottomHB.setManaged(true);
        });

        parent.getChildren().add(logoSP);
    }

    private TextField createTextFields(String promptText, String content, Pane parent, boolean isAddedTextListener) {
        VBox textFieldVB = new VBox();
        textFieldVB.setAlignment(Pos.TOP_LEFT);
        VBox.setMargin(textFieldVB, new Insets(7, 10, 7, 10));

        Label hintLB = new Label(promptText);
        hintLabels.add(hintLB);
        hintLB.getStyleClass().add("other-information");
        textFieldVB.getChildren().add(hintLB);
        hintLB.setUserData(true);
        hintLB.textProperty().addListener((ob, oldV, newV) -> {
            if (!promptText.equals(newV)) {
                hintLB.setStyle("-fx-text-fill: rgba(115,0,0);");
                hintLB.setUserData(false);
            } else {
                hintLB.setStyle("");
                hintLB.setUserData(true);
            }
        });

        TextField textField = new TextField(content);
        textField.setUserData(hintLB);
        textField.setPromptText(promptText);
        textField.getStyleClass().add("text-field");
        HelpfulStylingClass.applyPromptWithTF(textField);
        textFieldVB.getChildren().add(textField);

        if (isAddedTextListener) {
            textField.textProperty().addListener((ob, o, n) -> {
                if (n != null && !n.equals(o)) {
                    bottomHB.setVisible(true);
                    bottomHB.setManaged(true);
                }
            });
        }

        parent.getChildren().add(textFieldVB);
        return textField;
    }

    private void createBottomHB(Pane parent) {
        bottomHB = new HBox(10);
        bottomHB.setAlignment(Pos.BOTTOM_CENTER);
        VBox.setVgrow(bottomHB, Priority.ALWAYS);
        bottomHB.setPadding(new Insets(10, 10, 10, 10));
        bottomHB.setVisible(false);
        bottomHB.setManaged(false);
        bottomHB.setUserData("bottomHB");

        Button saveBtn = new Button("Сохранить");
        saveBtn.getStyleClass().add("login-button");
        bottomHB.getChildren().add(saveBtn);
        saveBtn.setOnAction(e -> {
            boolean isValid = true;
            for (Label label : hintLabels) {
                if (!((Boolean) label.getUserData())) {
                    isValid = false;
                    break;
                }
            }

            if (channelService.getChatWithName(newChannelNameUnique) != null
                    && !currentChannel.getChannel_name_unique().replace("@", "").equals(newChannelNameUnique)) {
                ((Label) channelNameUniqueTF.getUserData()).setText("Группа с таким именем уже существует");
                isValid = false;
            }

            if (isValid) {
                boolean isNewData = false;
                if (!Arrays.equals(newChannelLogo, currentChannel.getChannelLogo())) {
                    currentChannel.setChannelLogo(newChannelLogo);
                    isNewData = true;
                }

                if (!currentChannel.getChannelName().equals(newChannelName)) {
                    currentChannel.setChannelName(newChannelName);
                    isNewData = true;
                }

                if (!currentChannel.getChannel_name_unique().replace("@", "").equals(newChannelNameUnique)) {
                    currentChannel.setChannel_name_unique(newChannelNameUnique);
                    isNewData = true;
                }

                if (isNewData) {
                    Thread updateChannelThread = new Thread(() -> {
                        channelService.update(currentChannel);
                        List<ChannelUser> subscribers = channelUserService.getAllChannelUserByChannelID(currentChannel.getChannelID());
                        for (ChannelUser subscriber : subscribers) {
                            subscriber.setVisibleNameChat(currentChannel.getChannelName());
                            subscriber.setVisibleLogoChat(currentChannel.getChannelLogo());
                            channelUserService.update(subscriber);
                        }
                    });
                    updateChannelThread.setDaemon(true);
                    updateChannelThread.start();
                }

                bottomHB.setVisible(false);
                bottomHB.setManaged(false);

                resetAll();
            }
        });

        Button resetBtn = new Button("Сбросить");
        resetBtn.getStyleClass().add("login-button");
        bottomHB.getChildren().add(resetBtn);
        resetBtn.setOnAction(e -> {
            resetAll();
        });

        parent.getChildren().add(bottomHB);
    }

    private void resetAll() {
        newChannelLogo = currentChannel.getChannelLogo();
        newChannelName = currentChannel.getChannelName();
        newChannelNameUnique = currentChannel.getChannel_name_unique().replace("@", "");

        logoChannel.setImage(currentChannel.getPhotoImage());
        channelNameTF.setText(currentChannel.getChannelName());
        channelNameUniqueTF.setText(currentChannel.getChannel_name_unique().replace("@", ""));

        bottomHB.setVisible(false);
        bottomHB.setManaged(false);
    }
}
