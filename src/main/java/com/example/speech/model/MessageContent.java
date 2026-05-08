package com.example.speech.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "message_content", schema = "public")
public class MessageContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_content_id")
    private long messageContentId;
    @Column(name = "message_content_bytes")
    @Basic(fetch = FetchType.LAZY)
    private byte[] messageContentBytes;
    @Column(name = "message_content_file_name")
    private String messageContentFileName;
    @ManyToOne @JoinColumn(name = "message_id")
    private Message message;

    public MessageContent(){}

    public MessageContent(long messageContentId, byte[] messageContentBytes, String messageContentFileName, Message message) {
        this.messageContentId = messageContentId;
        this.messageContentBytes = messageContentBytes;
        this.messageContentFileName = messageContentFileName;
        this.message = message;
    }

    public long getMessageContentId() {
        return messageContentId;
    }

    public void setMessageContentId(long messageContentId) {
        this.messageContentId = messageContentId;
    }

    public byte[] getMessageContentBytes() {
        return messageContentBytes;
    }

    public void setMessageContentBytes(byte[] messageContentBytes) {
        this.messageContentBytes = messageContentBytes;
    }

    public String getMessageContentFileName() {
        return messageContentFileName;
    }

    public void setMessageContentFileName(String messageContentFileName) {
        this.messageContentFileName = messageContentFileName;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        MessageContent that = (MessageContent) object;
        return messageContentId == that.messageContentId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageContentId);
    }
}
