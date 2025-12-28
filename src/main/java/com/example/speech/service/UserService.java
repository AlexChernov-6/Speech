package com.example.speech.service;

import com.example.speech.model.User;
import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class UserService extends BaseService<User> {

    private SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();

    public UserService() {
        super(User.class);
    }

    public User getUserByEmail(String email) {
        String queryHQL = "from User where emailUser = :EMAIL_USER";

        try (Session session = sessionFactory.openSession()){
            return session.createQuery(queryHQL, User.class)
                    .setParameter("EMAIL_USER", email).uniqueResult();
        }
    }

    public User getUserByUserName(String userName) {
        String querySQL = "from User where nameUser = :NAME_USER";

        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(querySQL, User.class)
                    .setParameter("NAME_USER", userName).uniqueResult();
        }
    }
}
