package com.example.speech.model;

import com.example.speech.util.ImageConverter;
import jakarta.persistence.*;
import javafx.scene.image.Image;

import java.util.Objects;

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
    @ManyToOne @JoinColumn(name = "channel_type_id")
    private ChannelType channelType;
    @Column
    private String channel_name_unique;
    @Column
    private boolean disable_sharing;
    @ManyToOne @JoinColumn(name = "owner_id")
    private User ownerUser;

    @Transient
    private Image photoChannel;


    public Image getPhotoImage() {
        if (photoChannel == null && channelLogo != null) {
            try {
                photoChannel = ImageConverter.convertBytesToImage(channelLogo);
            } catch (Exception e) {
                System.err.println( e.getMessage());
                photoChannel = ImageConverter.getDefaultImage();
            }
        }
        return photoChannel != null ? photoChannel : ImageConverter.getDefaultImage();
    }

    public Channel() { }

    public Channel(int channelID, String channelName, byte[] channelLogo, int channelCountUser, ChannelType channelType, String channel_name_unique, boolean disable_sharing, User ownerUser) {
        this.channelID = channelID;
        this.channelName = channelName;
        this.channelLogo = channelLogo;
        this.channelCountUser = channelCountUser;
        this.channelType = channelType;
        this.channel_name_unique = channel_name_unique;
        this.disable_sharing = disable_sharing;
        this.ownerUser = ownerUser;
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
        photoChannel = null;
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

    public ChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    public String getChannel_name_unique() {
        return channel_name_unique;
    }

    public void setChannel_name_unique(String channel_name_unique) {
        this.channel_name_unique = channel_name_unique;
    }

    public boolean isDisable_sharing() {
        return disable_sharing;
    }

    public void setDisable_sharing(boolean disable_sharing) {
        this.disable_sharing = disable_sharing;
    }

    public User getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(User ownerUser) {
        this.ownerUser = ownerUser;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Channel channel = (Channel) object;
        return channelID == channel.channelID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(channelID);
    }
}
