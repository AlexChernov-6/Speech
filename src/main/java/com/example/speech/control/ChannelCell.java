package com.example.speech.control;

import com.example.speech.model.Channel;
import com.example.speech.util.ImageUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import static com.example.speech.util.ImageUtils.setCircularImage;

public class ChannelCell extends ListCell<Channel> {
    private final HBox rootHB;
    private ImageView logoCell;
    private Label modelNameLB, modelStateLB;

    private double logoSize = 45.0;

    public ChannelCell() {
        getStyleClass().setAll("chats-user-list-view");
        rootHB = new HBox(10);
        rootHB.setPadding(new Insets(5));
        rootHB.setPrefHeight(60);
        rootHB.setAlignment(Pos.CENTER_LEFT);

        logoCell = new ImageView();
        logoCell.setFitWidth(logoSize);
        logoCell.setFitHeight(logoSize);
        logoCell.setPreserveRatio(true);
        rootHB.getChildren().add(logoCell);

        Circle clip = new Circle();
        clip.setRadius(22.5);
        clip.setCenterX(22.5);
        clip.setCenterY(22.5);
        logoCell.setClip(clip);

        VBox rightVB = new VBox(5);
        HBox.setHgrow(rightVB, Priority.ALWAYS);
        rootHB.getChildren().add(rightVB);

        modelNameLB = new Label();
        modelNameLB.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        rightVB.getChildren().add(modelNameLB);

        modelStateLB = new Label();
        modelStateLB.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");
        rightVB.getChildren().add(modelStateLB);
    }

    @Override
    protected void updateItem(Channel channel, boolean empty) {
        super.updateItem(channel, empty);

        if(empty || channel == null) {
            setText(null);
            setGraphic(null);
        } else {
            setDataInCell(channel);
            setGraphic(rootHB);
        }
    }

    private void setDataInCell(Channel channel) {
        setCircularImage(logoCell, channel.getPhotoImage(), logoSize);
        modelNameLB.setText(channel.getChannel_name_unique());
        modelStateLB.setText(channel.getChannelType().getChannelTypeName());
    }
}
