package com.example.speech.service;

import com.example.speech.model.UsersInSilentMode;
import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UsersInSilentModeService extends BaseService<UsersInSilentMode> {
    public UsersInSilentModeService() {
        super(UsersInSilentMode.class);
    }

    public boolean isUserSetSilentMode(int channelID, int userID) {
        String queryHQL = "select count(*) from UsersInSilentMode where channelID = :CHANNEL_ID and userID = :USER_ID";
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            long result = session.createQuery(queryHQL, Long.class)
                    .setParameter("CHANNEL_ID", channelID)
                    .setParameter("USER_ID", userID)
                    .uniqueResult();
            return result != 0;
        }
    }

    public void deleteUserSetSilentMode(int channelID, int userID) {
        String queryHQL = "delete from UsersInSilentMode where channelID = :CHANNEL_ID and userID = :USER_ID";
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createQuery(queryHQL)
                    .setParameter("CHANNEL_ID", channelID)
                    .setParameter("USER_ID", userID)
                    .executeUpdate();
            transaction.commit();
        }
    }
}
