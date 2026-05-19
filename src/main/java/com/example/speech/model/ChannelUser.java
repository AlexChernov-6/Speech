package com.example.speech.model;

import jakarta.persistence.*;

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

    public ChannelUser() { }

    public ChannelUser(long channelUserId, User user, Channel channel) {
        this.channelUserId = channelUserId;
        this.user = user;
        this.channel = channel;
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
