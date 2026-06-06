package com.example.speech.service;

import com.example.speech.model.Channel;
import com.example.speech.model.ChannelType;
import org.hibernate.SessionFactory;

public class ChannelTypeService extends BaseService<ChannelType> {
    public ChannelTypeService() {
        super(ChannelType.class);
    }

    public ChannelTypeService(SessionFactory sessionFactory) {
        super(ChannelType.class, sessionFactory);
    }
}
