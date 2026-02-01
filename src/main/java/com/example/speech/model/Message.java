package com.example.speech.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenerationTime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "messages", schema = "public")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private long messageId;
    @Column(name = "message_datetime", updatable = false)
    @CreationTimestamp
    private LocalDateTime messageDatetime;
    @ManyToOne @JoinColumn(name = "channel_user_id")
    private ChannelUser channelUser;
    @Column(name = "message_content")
    private byte[] messageContent;
    @Column(name = "message_status", updatable = false)
    private String messageStatus = "отправлено";
    @Column(name = "deleted_by_users")
    private List<Long> deletedByUsers;

    public Message() { }

    public Message(long messageId, LocalDateTime messageDatetime, ChannelUser channelUser, byte[] messageContent, String messageStatus) {
        this.messageId = messageId;
        this.messageDatetime = messageDatetime;
        this.channelUser = channelUser;
        this.messageContent = messageContent;
        this.messageStatus = messageStatus;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public LocalDateTime getMessageDatetime() {
        return messageDatetime;
    }

    public void setMessageDatetime(LocalDateTime messageDatetime) {
        this.messageDatetime = messageDatetime;
    }

    public ChannelUser getChannelUser() {
        return channelUser;
    }

    public void setChannelUser(ChannelUser channelUser) {
        this.channelUser = channelUser;
    }

    public byte[] getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(byte[] messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public List<Long> getDeletedByUsers() {
        if (deletedByUsers == null)
            deletedByUsers = new ArrayList<>();
        return deletedByUsers;
    }

    public void setDeletedByUsers(List<Long> deletedByUsers) {
        this.deletedByUsers = deletedByUsers;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Message message = (Message) object;
        return messageId == message.messageId && Objects.equals(messageDatetime, message.messageDatetime) && Objects.equals(channelUser, message.channelUser) && Objects.deepEquals(messageContent, message.messageContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, messageDatetime, channelUser, Arrays.hashCode(messageContent));
    }
}
