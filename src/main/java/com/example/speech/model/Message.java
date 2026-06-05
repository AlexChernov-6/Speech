package com.example.speech.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenerationTime;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

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
    @Column(name = "message_status")
    private String messageStatus;
    @Column(name = "deleted_by_users")
    private List<Long> deletedByUsers;
    @Column(name = "modified_message", columnDefinition = "boolean default false")
    private Boolean modifiedMessage = false;
    @Column(name = "message_id_reply_to")
    private Long messageIdReplyTo;
    @Column(name = "pin_message", columnDefinition = "boolean default false")
    private Boolean pinMessage = false;
    @Column(name = "forwarded_from")
    private Long forwardedFrom;
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<MessageContent> messageContent = new ArrayList<>();
    @Column(name = "message_string")
    private byte[] messageString;
    @ManyToOne @JoinColumn(name = "id_channel_invitations")
    private Channel channelInvitations;

    public Message() { }

    public Message(long messageId, LocalDateTime messageDatetime, ChannelUser channelUser, String messageStatus, List<Long> deletedByUsers, Boolean modifiedMessage, Long messageIdReplyTo, Boolean pinMessage, Long forwardedFrom, List<MessageContent> messageContent, byte[] messageString, Channel channelInvitations) {
        this.messageId = messageId;
        this.messageDatetime = messageDatetime;
        this.channelUser = channelUser;
        this.messageStatus = messageStatus;
        this.deletedByUsers = deletedByUsers;
        this.modifiedMessage = modifiedMessage;
        this.messageIdReplyTo = messageIdReplyTo;
        this.pinMessage = pinMessage;
        this.forwardedFrom = forwardedFrom;
        this.messageContent = messageContent;
        this.messageString = messageString;
        this.channelInvitations = channelInvitations;
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

    public Boolean isModifiedMessage() {
        return modifiedMessage;
    }

    public void setModifiedMessage(Boolean modifiedMessage) {
        this.modifiedMessage = modifiedMessage;
    }

    public Long getMessageIdReplyTo() {
        return messageIdReplyTo;
    }

    public void setMessageIdReplyTo(Long messageIdReplyTo) {
        this.messageIdReplyTo = messageIdReplyTo;
    }

    public Boolean getPinMessage() {
        return pinMessage;
    }

    public void setPinMessage(Boolean pinMessage) {
        this.pinMessage = pinMessage;
    }

    public Long getForwardedFrom() {
        return forwardedFrom;
    }

    public void setForwardedFrom(Long forwardedFrom) {
        this.forwardedFrom = forwardedFrom;
    }

    public List<MessageContent> getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(List<MessageContent> mContent) {
        messageContent.clear();
        for(MessageContent mC : mContent)
            mC.setMessage(this);
        this.messageContent.addAll(mContent);
    }

    public void addMessageContent(MessageContent messageContent) {
        this.messageContent.addLast(messageContent);
        messageContent.setMessage(this);
    }

    public String getMessageString() {
        return new String(messageString, StandardCharsets.UTF_8);
    }

    public void setMessageString(String messageString) {
        this.messageString = messageString.getBytes();
    }

    public Channel getChannelInvitations() {
        return channelInvitations;
    }

    public void setChannelInvitations(Channel channelInvitations) {
        this.channelInvitations = channelInvitations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(messageId, message.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }
}
