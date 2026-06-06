package com.example.speech.service;

import com.example.speech.model.Channel;
import com.example.speech.model.User;
import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class ChannelService extends BaseService<Channel> {
    public ChannelService() {
        super(Channel.class);
    }

    public ChannelService(SessionFactory sessionFactory) {
        super(Channel.class, sessionFactory);
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
        String queryHQL = "from Channel where channel_name_unique = '@' || :CHANNEL_NAME_UNIQUE";

        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            return session.createQuery(queryHQL, Channel.class).setParameter("CHANNEL_NAME_UNIQUE", name).uniqueResult();
        }
    }

    public List<User> getAllUserInChannel(long channelID) {
        String queryHQL = "select user from ChannelUser where channel.channelID = :CHANNEL_ID";

        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            return session.createQuery(queryHQL, User.class)
                    .setParameter("CHANNEL_ID", channelID).list();
        }
    }

    public Channel getChannelByTwoUser(User user1, User user2) {
        String channelName1 = user1.getNameUser() + "_" + user2.getNameUser();
        String channelName2 = user2.getNameUser() + "_" + user1.getNameUser();
        String queryHQL = "from Channel where channel_name_unique = :CHANNEL_NAME_ONE or channel_name_unique = :CHANNEL_NAME_TWO";

        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            return session.createQuery(queryHQL, Channel.class)
                    .setParameter("CHANNEL_NAME_ONE", channelName1)
                    .setParameter("CHANNEL_NAME_TWO", channelName2)
                    .uniqueResult();
        }
    }

    public boolean isDisableSharing(Channel channel) {
        String queryHQL = "select disable_sharing from Channel where channelID = :CHANNEL_ID";
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            return session.createQuery(queryHQL, Boolean.class)
                    .setParameter("CHANNEL_ID", channel.getChannelID())
                    .uniqueResult();
        }
    }
}
