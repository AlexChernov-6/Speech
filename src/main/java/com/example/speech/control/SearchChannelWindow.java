package com.example.speech.control;

import com.example.speech.model.Channel;
import com.example.speech.model.ChannelType;
import com.example.speech.model.ChannelUser;
import com.example.speech.model.User;
import com.example.speech.service.BaseService;
import com.example.speech.service.ChannelService;
import com.example.speech.service.ChannelTypeService;
import com.example.speech.service.ChannelUserService;
import com.example.speech.util.HelpfulStylingClass;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.List;

public class SearchChannelWindow extends VBox {
    private SpeechBaseController speechBaseController;

    private final Pane shadowPane;
    private TextFlow hintTextFlow;
    private Label emptyLB;
    private TextField searchTF;

    private FilteredList<Channel> channelFilteredList;
    private ListView<Channel> channelListView;

    private FilteredList<User> userFilteredList;
    private ListView<User> userListView;

    private Object selectedObject;

    private Button joinChannelBtn;

    private final ChannelUserService channelUserService = new ChannelUserService();

    private ChannelType correspondenceChannelType = new ChannelTypeService().getRowById(3L);

    public SearchChannelWindow(SpeechBaseController speechBaseController) {
        this.speechBaseController = speechBaseController;

        StackPane parentStackPane = speechBaseController.getMessagesSP();

        shadowPane = new Pane();
        shadowPane.setVisible(false);
        shadowPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        parentStackPane.getChildren().add(shadowPane);

        maxWidthProperty().bind(parentStackPane.widthProperty().subtract(200));
        maxHeightProperty().bind(parentStackPane.heightProperty().subtract(200));
        setAlignment(Pos.TOP_CENTER);
        setOpacity(0.0);
        setSpacing(5);

        createInfoLB();

        createMainVB();

        parentStackPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!isVisible()) return;

            Point2D pointInWindow = screenToLocal(event.getScreenX(), event.getScreenY());
            if (pointInWindow != null && contains(pointInWindow))
                return;
            else if (this.getOpacity() == 1.0)
                hide();
        });

        parentStackPane.getChildren().add(this);
    }

    private void createInfoLB() {
        Label infoLB = new Label("Поиск серверов, каналов и ЛС");
        infoLB.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20;");
        getChildren().add(infoLB);
    }

    private void createMainVB() {
        VBox mainVB = new VBox(15);
        mainVB.setStyle("-fx-background-radius: 12; -fx-background-color: rgba(55,55,55); -fx-border-radius: 12;" +
                " -fx-border-weight: 0.5; -fx-border-color: rgba(120, 120, 120);");
        VBox.setVgrow(mainVB, Priority.ALWAYS);

        mainVB.getChildren().addAll(createSearchTF(), createCentralAnchorPane(), createHintTextFlow(), createJoinChannelBtn());

        joinChannelBtn.prefWidthProperty().bind(mainVB.widthProperty().subtract(10 + 10));

        getChildren().add(mainVB);
    }

    private TextField createSearchTF() {
        searchTF = new TextField();
        searchTF.setPromptText("Куда отправимся?");
        HelpfulStylingClass.applyPromptWithTF(searchTF);
        searchTF.getStyleClass().setAll("search-text-field");
        VBox.setMargin(searchTF, new Insets(10, 10, 0, 10));

        searchTF.textProperty().addListener((ob, oldV, newV) -> {
            if (newV == null || newV.isEmpty() || newV.equals("Куда отправимся?")) {
                if (channelListView.isVisible()) {
                    channelListView.setVisible(false);
                    channelListView.setManaged(false);
                }

                if (userListView.isVisible()) {
                    userListView.setVisible(false);
                    userListView.setManaged(false);
                }

                channelFilteredList.setPredicate(ch -> true);
                userFilteredList.setPredicate(us -> true);

                emptyLB.setVisible(true);
                emptyLB.setManaged(true);
            } else {
                emptyLB.setVisible(false);
                emptyLB.setManaged(false);

                if (newV.trim().charAt(0) == '@') {
                    channelListView.setVisible(true);
                    channelListView.setManaged(true);

                    channelFilteredList.setPredicate(channel -> channel.getChannel_name_unique().toLowerCase().contains(newV.toLowerCase()));
                } else {
                    userListView.setVisible(true);
                    userListView.setManaged(true);

                    userFilteredList.setPredicate(user -> user.getNameUser().toLowerCase().contains(newV.toLowerCase()));
                }
            }
        });

        return searchTF;
    }

    private AnchorPane createCentralAnchorPane() {
        AnchorPane centralAnchorPane = new AnchorPane();
        VBox.setVgrow(centralAnchorPane, Priority.ALWAYS);

        centralAnchorPane.getChildren().addAll(createEmptyLabel(), createChannelListView(), createUserListView());

        return centralAnchorPane;
    }

    private Label createEmptyLabel() {
        emptyLB = new Label("Введите название канала или пользователя");
        AnchorPane.setTopAnchor(emptyLB, 0.0);
        AnchorPane.setRightAnchor(emptyLB, 0.0);
        AnchorPane.setBottomAnchor(emptyLB, 0.0);
        AnchorPane.setLeftAnchor(emptyLB, 0.0);
        emptyLB.setAlignment(Pos.CENTER);
        emptyLB.setStyle("-fx-font-weight: bold; -fx-text-fill: rgba(150, 150 ,150); -fx-font-size: 20;");

        return emptyLB;
    }

    private ListView<Channel> createChannelListView() {
        channelListView = new ListView<>();
        channelFilteredList = setStateListView(channelListView, e -> new ChannelCell(), Channel.class);

        return channelListView;
    }

    private ListView<User> createUserListView() {
        userListView = new ListView<>();
        userFilteredList = setStateListView(userListView, e -> new UserCell(), User.class);

        return userListView;
    }

    private <T> FilteredList<T> setStateListView(ListView<T> listView, Callback<ListView<T>, ListCell<T>> cellFactory, Class<T> model) {
        listView.setCellFactory(cellFactory);
        listView.getStyleClass().setAll("search-list-view");
        AnchorPane.setTopAnchor(listView, 0.0);
        AnchorPane.setRightAnchor(listView, 10.0);
        AnchorPane.setBottomAnchor(listView, 0.0);
        AnchorPane.setLeftAnchor(listView, 10.0);
        listView.setManaged(false);
        listView.setVisible(false);

        listView.getSelectionModel().selectedItemProperty().addListener((ob, oldV, newV) -> {
            if (newV != null) {
                hintTextFlow.setVisible(false);
                hintTextFlow.setManaged(false);

                joinChannelBtn.setVisible(true);
                joinChannelBtn.setManaged(true);

                selectedObject = newV;

                if (selectedObject instanceof Channel) {
                    joinChannelBtn.setText("Вступить в " + ((Channel) selectedObject).getChannel_name_unique());//подписывать на канал изменения
                    joinChannelBtn.setOnAction(e -> {
                        ChannelUser oldChannelUser = channelUserService.getChannelUserByUserIdAndChannelId(
                                speechBaseController.getCurrentUser().getIdUser(), ((Channel) selectedObject).getChannelID());
                        if(oldChannelUser == null) {
                            ChannelUser newChannelUser = new ChannelUser();
                            newChannelUser.setUser(speechBaseController.getCurrentUser());
                            newChannelUser.setChannel((Channel) selectedObject);

                            speechBaseController.getMessageListener().addChannelAsync(newChannelUser);

                            boolean save = channelUserService.save(newChannelUser);

                            if (save)
                                speechBaseController.userChats.add(newChannelUser);

                            speechBaseController.chatsView.getSelectionModel().select(newChannelUser);

                        } else {
                            if(!speechBaseController.userChats.contains(oldChannelUser))
                                speechBaseController.userChats.add(oldChannelUser);

                            speechBaseController.getMessageListener().addChannelAsync(oldChannelUser);

                            speechBaseController.chatsView.getSelectionModel().select(oldChannelUser);
                        }

                        searchTF.setText("");
                        hide();
                    });
                } else if (selectedObject instanceof User) {
                    joinChannelBtn.setText("Добавить " + ((User) selectedObject).getNameUser());
                    joinChannelBtn.setOnAction(e -> {
                        ChannelService channelService = new ChannelService();
                        if (!channelService.chatsWithThatName(speechBaseController.getCurrentUser().getNameUser() + "_" + ((User) selectedObject).getNameUser()) &&
                                !channelService.chatsWithThatName(((User) selectedObject).getNameUser() + "_" + speechBaseController.getCurrentUser().getNameUser())) {
                            Channel channel = new Channel();
                            channel.setChannelName(((User) selectedObject).getVisibleNameUser());
                            channel.setChannelLogo(((User) selectedObject).getPhotoUser());
                            channel.setChannelType(correspondenceChannelType);
                            channel.setChannel_name_unique(speechBaseController.getCurrentUser().getNameUser() + "_" + ((User) selectedObject).getNameUser());
                            channel.setDisable_sharing(false);

                            channelService.save(channel);

                            ChannelUser newChannelUser1 = new ChannelUser();
                            newChannelUser1.setUser(speechBaseController.getCurrentUser());
                            newChannelUser1.setChannel(channel);
                            speechBaseController.getMessageListener().addChannelAsync(newChannelUser1);

                            ChannelUser newChannelUser2 = new ChannelUser();
                            newChannelUser2.setUser(((User) selectedObject));
                            newChannelUser2.setChannel(channel);

                            boolean save = channelUserService.save(newChannelUser1);

                            channelUserService.save(newChannelUser2);

                            if (save)
                                speechBaseController.userChats.add(newChannelUser1);

                            speechBaseController.chatsView.getSelectionModel().select(newChannelUser1);
                            searchTF.setText("");
                            hide();
                        } else {
                            Channel oldChannel;
                            if (channelService.chatsWithThatName(speechBaseController.getCurrentUser().getNameUser() + "_" + ((User) selectedObject).getNameUser()))
                                oldChannel = channelService.getChatWithName(speechBaseController.getCurrentUser().getNameUser() + "_" + ((User) selectedObject).getNameUser());
                            else
                                oldChannel = channelService.getChatWithName(((User) selectedObject).getNameUser() + "_" + speechBaseController.getCurrentUser().getNameUser());

                            ChannelUser oldChannelUser1 = channelUserService.getChannelUserByUserIdAndChannelId(
                                    speechBaseController.getCurrentUser().getIdUser(), oldChannel.getChannelID());

                            if(oldChannelUser1 != null) {
                                if (!speechBaseController.userChats.contains(oldChannelUser1))
                                    speechBaseController.userChats.add(oldChannelUser1);

                                speechBaseController.getMessageListener().addChannelAsync(oldChannelUser1);

                                speechBaseController.chatsView.getSelectionModel().select(oldChannelUser1);
                            } else {
                                ChannelUser newChannelUser = new ChannelUser();
                                newChannelUser.setUser(speechBaseController.getCurrentUser());
                                newChannelUser.setChannel(oldChannel);

                                speechBaseController.getMessageListener().addChannelAsync(newChannelUser);

                                boolean save = channelUserService.save(newChannelUser);

                                if (save)
                                    speechBaseController.userChats.add(newChannelUser);

                                speechBaseController.chatsView.getSelectionModel().select(newChannelUser);
                            }

                            ChannelUser oldChannelUser2 = channelUserService.getChannelUserByUserIdAndChannelId(
                                    ((User) selectedObject).getIdUser(), oldChannel.getChannelID());

                            if(oldChannelUser2 == null) {
                                ChannelUser newChannelUser = new ChannelUser();
                                newChannelUser.setUser(((User) selectedObject));
                                newChannelUser.setChannel(oldChannel);

                                channelUserService.save(newChannelUser);
                            }

                            searchTF.setText("");
                            hide();
                        }
                    });
                }
            }
        });

        listView.visibleProperty().addListener((ob, oldV, newV) -> {
            if (oldV && !newV) {
                hintTextFlow.setVisible(true);
                hintTextFlow.setManaged(true);

                joinChannelBtn.setVisible(false);
                joinChannelBtn.setManaged(false);
            }
        });

        ObservableList<T> list;
        if(model == User.class)
            list = (ObservableList<T>) FXCollections.observableList(((List<User>) new BaseService<>(model).getAllRow())
                    .stream().filter(u -> !u.equals(speechBaseController.getCurrentUser())).toList());
        else
            list = FXCollections.observableList(new BaseService<>(model).getAllRow());

        FilteredList<T> filteredList = new FilteredList<>(list, t -> true);

        listView.setItems(filteredList);

        return filteredList;
    }

    private TextFlow createHintTextFlow() {
        hintTextFlow = new TextFlow();
        VBox.setMargin(hintTextFlow, new Insets(0, 10, 10, 10));

        Label firstText = new Label("ПОДСКАЗКА: ");
        firstText.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 10; -fx-padding: 0;");
        hintTextFlow.getChildren().add(firstText);

        Label secondText = new Label("для поиска канала вначале используйте ");
        secondText.setStyle("-fx-text-fill: white; -fx-font-size: 10; -fx-padding: 0;");
        hintTextFlow.getChildren().add(secondText);

        Label thirdText = new Label("@");
        thirdText.setStyle("-fx-text-fill: white; -fx-font-size: 10; -fx-padding: 0 1; -fx-background-color: black; -fx-background-radius: 8;");
        hintTextFlow.getChildren().add(thirdText);

        return hintTextFlow;
    }

    private Button createJoinChannelBtn() {
        joinChannelBtn = new Button();
        joinChannelBtn.setVisible(false);
        joinChannelBtn.setManaged(false);
        joinChannelBtn.getStyleClass().setAll("login-button");
        joinChannelBtn.setAlignment(Pos.CENTER);
        VBox.setMargin(joinChannelBtn, new Insets(0, 10, 10, 10));

        return joinChannelBtn;
    }

    public void show() {
        shadowPane.setVisible(true);
        setManaged(true);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), this);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public void hide() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            shadowPane.setVisible(false);
            setManaged(false);
        });
        fadeOut.play();
    }
}
