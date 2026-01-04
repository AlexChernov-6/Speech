package com.example.speech.model;

import jakarta.persistence.*;

@Entity
@Table(name = "channel_types", schema = "public")
public class ChannelType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_type_id")
    private int channelTypeId;
    @Column(name = "channel_type_name")
    private String channelTypeName;

    public int getChannelTypeId() {
        return channelTypeId;
    }

    public void setChannelTypeId(int channelTypeId) {
        this.channelTypeId = channelTypeId;
    }

    public String getChannelTypeName() {
        return channelTypeName;
    }

    public void setChannelTypeName(String channelTypeName) {
        this.channelTypeName = channelTypeName;
    }
}
