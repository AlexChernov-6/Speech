package com.example.speech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users_in_silent_mode", schema = "public")
public class UsersInSilentMode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_in_silent_mode_id")
    private long usersInSilentModeId;
    @Column(name = "channel_id")
    private int channelID;
    @Column(name = "user_id")
    private int userID;

    public UsersInSilentMode() { }

    public UsersInSilentMode(long usersInSilentModeId, int channelID, int userID) {
        this.usersInSilentModeId = usersInSilentModeId;
        this.channelID = channelID;
        this.userID = userID;
    }

    public long getUsersInSilentModeId() {
        return usersInSilentModeId;
    }

    public void setUsersInSilentModeId(long usersInSilentModeId) {
        this.usersInSilentModeId = usersInSilentModeId;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
