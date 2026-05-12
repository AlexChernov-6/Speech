package com.example.speech.service;

import com.example.speech.model.Channel;
import com.example.speech.model.ChannelUser;
import com.example.speech.model.User;
import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class ChannelUserService extends BaseService<ChannelUser> {
    private static SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();

    public ChannelUserService() {
        super(ChannelUser.class);
    }

    public List<ChannelUser> getAllChatsByUser(User user) {
        String queryHQL = "from ChannelUser where user.idUser = :USER_ID";
        try (Session session = sessionFactory.openSession()){
            return session.createQuery(queryHQL, ChannelUser.class)
                    .setParameter("USER_ID", user.getIdUser())
                    .list();
        }
    }

    public String getInterlocutorStatus(Channel channel, User user) {
        String queryHQL = "select user.statusUser from ChannelUser where channel.channelID = :CHANEL_ID " +
                "and user.idUser != :USER_ID";
        try (Session session = sessionFactory.openSession()){
            return session.createQuery(queryHQL, String.class)
                    .setParameter("CHANEL_ID", channel.getChannelID())
                    .setParameter("USER_ID", user.getIdUser())
                    .uniqueResult();
        }
    }

    public ChannelUser getChannelUserByUserIdAndChannelId(long userId, long channelId) {
        String queryHQL = "from ChannelUser where user.idUser = :USER_ID and channel.channelID = :CHANNEL_ID";
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            return session.createQuery(queryHQL, ChannelUser.class)
                    .setParameter("USER_ID", userId)
                    .setParameter("CHANNEL_ID", channelId)
                    .uniqueResult();
        }
    }
}
