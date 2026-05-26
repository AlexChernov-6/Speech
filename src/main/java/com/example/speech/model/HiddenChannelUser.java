package com.example.speech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hidden_channel_user", schema = "public")
public class HiddenChannelUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hidden_channel_user_id")
    private long hiddenChannelUserID;
    @ManyToOne @JoinColumn(name = "hidden_channel_id")
    private Channel channel;
    @ManyToOne @JoinColumn(name = "hidden_user_id")
    private User user;

    public HiddenChannelUser() { }

    public HiddenChannelUser(long hiddenChannelUserID, Channel channel, User user) {
        this.hiddenChannelUserID = hiddenChannelUserID;
        this.channel = channel;
        this.user = user;
    }

    public long getHiddenChannelUserID() {
        return hiddenChannelUserID;
    }

    public void setHiddenChannelUserID(long hiddenChannelUserID) {
        this.hiddenChannelUserID = hiddenChannelUserID;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
