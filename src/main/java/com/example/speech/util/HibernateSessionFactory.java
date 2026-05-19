package com.example.speech.util;

import com.example.speech.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HibernateSessionFactory {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try (FileInputStream fileInputStream = new FileInputStream("config.properties")){
                Properties properties = new Properties();
                properties.load(fileInputStream);
                sessionFactory = new Configuration().setProperties(properties)
                        .addAnnotatedClass(User.class)
                        .addAnnotatedClass(Channel.class)
                        .addAnnotatedClass(ChannelUser.class)
                        .addAnnotatedClass(ChannelType.class)
                        .addAnnotatedClass(Message.class)
                        .addAnnotatedClass(MessageContent.class)
                        .addAnnotatedClass(UserMessageRead.class)
                        .buildSessionFactory();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return sessionFactory;
    }
}
