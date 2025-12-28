package com.example.speech.util;

import com.example.speech.model.Channel;
import com.example.speech.model.ChannelUser;
import com.example.speech.model.User;
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
                        .buildSessionFactory();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return sessionFactory;
    }
}
