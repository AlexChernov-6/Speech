package com.example.speech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "channels", schema = "public")
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private int channelID;
    @Column(name = "channel_name")
    private String channelName;
    @Column(name = "channel_photo")
    private byte[] channelLogo;
    @Column(name = "channel_count_user")
    private int channelCountUser;

    public Channel() { }

    public Channel(int channelID, String channelName, byte[] channelLogo) {
        this.channelID = channelID;
        this.channelName = channelName;
        this.channelLogo = channelLogo;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public byte[] getChannelLogo() {
        return channelLogo;
    }

    public void setChannelLogo(byte[] channelLogo) {
        this.channelLogo = channelLogo;
    }

    public int getChannelCountUser() {
        return channelCountUser;
    }

    public void setChannelCountUser(int channelCountUser) {
        this.channelCountUser = channelCountUser;
    }
}
