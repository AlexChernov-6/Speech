package com.example.speech.service;

import com.example.speech.model.Channel;
import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.Session;

public class ChannelService extends BaseService<Channel> {
    public ChannelService() {
        super(Channel.class);
    }

    public boolean chatsWithThatName(String name) {
        String queryHQL = "select count(*) from Channel where channel_name_unique = :CHANNEL_NAME_UNIQUE";

        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            long result = session.createQuery(queryHQL, Long.class).setParameter("CHANNEL_NAME_UNIQUE", name)
                    .uniqueResult();

            return result > 0;
        }
    }

    public Channel getChatWithName(String name) {
        String queryHQL = "from Channel where channel_name_unique = :CHANNEL_NAME_UNIQUE";

        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            return session.createQuery(queryHQL, Channel.class).setParameter("CHANNEL_NAME_UNIQUE", name).uniqueResult();
        }
    }
}
