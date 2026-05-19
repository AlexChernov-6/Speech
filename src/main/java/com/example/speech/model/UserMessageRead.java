package com.example.speech.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_message_read", schema = "public")
public class UserMessageRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_message_read_id")
    private long userMessageReadID;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "message_id")
    private Message message;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "read_at")
    private LocalDateTime readAt = LocalDateTime.now();

    public UserMessageRead() { }

    public UserMessageRead(long userMessageReadID, Message message, User user, LocalDateTime readAt) {
        this.userMessageReadID = userMessageReadID;
        this.message = message;
        this.user = user;
        this.readAt = readAt;
    }

    public long getUserMessageReadID() {
        return userMessageReadID;
    }

    public void setUserMessageReadID(long userMessageReadID) {
        this.userMessageReadID = userMessageReadID;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        UserMessageRead that = (UserMessageRead) object;
        return userMessageReadID == that.userMessageReadID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userMessageReadID);
    }
}
