package com.example.speech.model;

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

    @Transient
    private Image imageBackground;

    public ChannelUser() { }

    public ChannelUser(long channelUserId, User user, Channel channel, byte[] backgroundImage) {
        this.channelUserId = channelUserId;
        this.user = user;
        this.channel = channel;
        this.backgroundImage = backgroundImage;
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
