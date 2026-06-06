package com.example.speech.service;

import com.example.speech.model.HiddenChannelUser;
import com.example.speech.model.MessageContent;
import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class MessageContentService extends BaseService<MessageContent> {
    public MessageContentService() {
        super(MessageContent.class);
    }

    public MessageContentService(SessionFactory sessionFactory) {
        super(MessageContent.class, sessionFactory);
    }

    public byte[] getContentBytes(long contentId) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            MessageContent content = session.find(MessageContent.class, contentId);
            if (content != null) {
                return content.getMessageContentBytes();
            }
            return null;
        }
    }
}
