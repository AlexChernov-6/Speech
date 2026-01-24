package com.example.speech.service;

import com.example.speech.model.Message;
import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.Session;

import java.util.List;

public class MessageService extends BaseService<Message> {
    public MessageService() {
        super(Message.class);
    }

    public List<Message> getAllMessageInChannel(int channelId) {
        String queryHQL = "from Message where channelUser.channel.channelID = :CHANNEL_ID order by messageDatetime";

        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            return session.createQuery(queryHQL, Message.class)
                    .setParameter("CHANNEL_ID", channelId)
                    .list();
        }
    }
}
