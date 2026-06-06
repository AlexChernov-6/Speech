package com.example.speech.service;

import com.example.speech.model.Channel;
import com.example.speech.model.ChannelUser;
import com.example.speech.model.HiddenChannelUser;
import com.example.speech.model.User;
import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class HiddenChannelUserService extends BaseService<HiddenChannelUser> {
    public HiddenChannelUserService() {
        super(HiddenChannelUser.class);
    }

    public HiddenChannelUserService(SessionFactory sessionFactory) {
        super(HiddenChannelUser.class, sessionFactory);
    }

    public HiddenChannelUser isHiddenUserFromChannel(Channel channel, User user) {
        String queryHQL = "from HiddenChannelUser where channel.channelID = :CHANNEL_ID and user.idUser = :USER_ID";
        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            return session.createQuery(queryHQL, HiddenChannelUser.class)
                    .setParameter("CHANNEL_ID", channel.getChannelID())
                    .setParameter("USER_ID", user.getIdUser())
                    .uniqueResult();
        }
    }
}
