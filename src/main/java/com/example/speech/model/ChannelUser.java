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

    @Transient
    private Image imageBackground;

    @Transient
    private Image photoImage;

    public ChannelUser() { }

    public ChannelUser(long channelUserId, User user, Channel channel, byte[] backgroundImage, String visibleNameChat, byte[] visibleLogoChat, Image imageBackground) {
        this.channelUserId = channelUserId;
        this.user = user;
        this.channel = channel;
        this.backgroundImage = backgroundImage;
        this.visibleNameChat = visibleNameChat;
        this.visibleLogoChat = visibleLogoChat;
        this.imageBackground = imageBackground;
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
