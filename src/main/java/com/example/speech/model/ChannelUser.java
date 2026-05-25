package com.example.speech.model;

import com.example.speech.util.ImageConverter;
import jakarta.persistence.*;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

@Entity
@Table(name = "channel_user", schema = "public")
public class ChannelUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_user_id")
    private long channelUserId;
    @ManyToOne @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne @JoinColumn(name = "channel_id")
    private Channel channel;
    @Column(name = "background_image")
    private byte[] backgroundImage;
    @Column(name = "visible_name_chat")
    private String visibleNameChat;
    @Column(name = "visible_logo_chat")
    private byte[] visibleLogoChat;
    @Column(name = "status_of_the_interlocutor")
    private String statusOfTheInterlocutor;

    @Transient
    private Image imageBackground;

    @Transient
    private Image photoImage;

    public ChannelUser() { }

    public ChannelUser(String statusOfTheInterlocutor, byte[] visibleLogoChat, String visibleNameChat, byte[] backgroundImage, Channel channel, User user, long channelUserId) {
        this.statusOfTheInterlocutor = statusOfTheInterlocutor;
        this.visibleLogoChat = visibleLogoChat;
        this.visibleNameChat = visibleNameChat;
        this.backgroundImage = backgroundImage;
        this.channel = channel;
        this.user = user;
        this.channelUserId = channelUserId;
    }

    public long getChannelUserId() {
        return channelUserId;
    }

    public void setChannelUserId(long channelUserId) {
        this.channelUserId = channelUserId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public byte[] getBackgroundImageBytes() {
        return backgroundImage;
    }

    public void setBackgroundImageBytes(byte[] backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public Image getBackgroundImage() {
        if(imageBackground == null)
            imageBackground = new Image(new ByteArrayInputStream(backgroundImage));

        return imageBackground;
    }

    public void setBackgroundImage(Image imageBackground) {
        if(imageBackground.equals(this.imageBackground))
            return;
        this.imageBackground = imageBackground;
    }

    public String getVisibleNameChat() {
        return visibleNameChat;
    }

    public void setVisibleNameChat(String visibleNameChat) {
        this.visibleNameChat = visibleNameChat;
    }

    public byte[] getVisibleLogoChat() {
        return visibleLogoChat;
    }

    public void setVisibleLogoChat(byte[] visibleLogoChat) {
        this.visibleLogoChat = visibleLogoChat;
    }

    public Image getPhotoImage() {
        if (photoImage == null && visibleLogoChat != null) {
            try {
                photoImage = ImageConverter.convertBytesToImage(visibleLogoChat);
            } catch (Exception e) {
                System.err.println( e.getMessage());
                photoImage = ImageConverter.getDefaultImage();
            }
        }
        return photoImage != null ? photoImage : ImageConverter.getDefaultImage();
    }

    public String getStatusOfTheInterlocutor() {
        return statusOfTheInterlocutor;
    }

    public void setStatusOfTheInterlocutor(String statusOfTheInterlocutor) {
        this.statusOfTheInterlocutor = statusOfTheInterlocutor;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ChannelUser that = (ChannelUser) object;
        return channelUserId == that.channelUserId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(channelUserId);
    }
}
