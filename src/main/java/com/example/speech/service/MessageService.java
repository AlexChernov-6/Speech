package com.example.speech.service;

import com.example.speech.model.Message;
import com.example.speech.model.MessageContent;
import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageService extends BaseService<Message> {
    public MessageService() {
        super(Message.class);
    }

    public MessageService(SessionFactory sessionFactory) {
        super(Message.class, sessionFactory);
    }

    public List<Message> getAllMessageInChannel(int channelId) {
        String queryHQL = "from Message where channelUser.channel.channelID = :CHANNEL_ID order by messageDatetime";

        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            return session.createQuery(queryHQL, Message.class)
                    .setParameter("CHANNEL_ID", channelId)
                    .list();
        }
    }

    public void unpinAllMessageInChannel(int channelId) {
        String nativeSQL = "UPDATE messages m " +
                "SET pin_message = false " +
                "FROM channel_user cu " +
                "WHERE m.channel_user_id = cu.channel_user_id " +
                "AND cu.channel_id = :channelId " +
                "AND m.pin_message = true";

        Transaction transaction = null;
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            session.createNativeQuery(nativeSQL)
                    .setParameter("channelId", channelId)
                    .executeUpdate();

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Ошибка при откреплении сообщений", e);
        }
    }

    /**
     * Удаляет сообщение вместе со всеми вложениями (каскад + orphanRemoval).
     */
    public void deleteAllMessage(List<Message> messages) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        try {
            session.getTransaction().begin();
            for(Message msg : messages)
                session.remove(msg);   // Hibernate сам удалит все MessageContent
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction().isActive())
                session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}
