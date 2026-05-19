package com.example.speech.service;

import com.example.speech.model.UserMessageRead;
import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.Session;

public class UserMessageReadService extends BaseService<UserMessageRead> {
    public UserMessageReadService() {
        super(UserMessageRead.class);
    }

    public UserMessageRead anyMatchByMessageIdAndUserId(long messageID, int userID) {
        String hql = "from UserMessageRead where message.messageId = :MESSAGE_ID and user.idUser = :USER_ID";

        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            return session.createQuery(hql, UserMessageRead.class)
                    .setParameter("MESSAGE_ID", messageID)
                    .setParameter("USER_ID", userID)
                    .uniqueResult();
        }
    }
}
