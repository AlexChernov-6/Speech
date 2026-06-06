package com.example.speech.service;

import com.example.speech.model.Admin;
import com.example.speech.util.HibernateAdminSessionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class AdminService extends BaseService<Admin> {
    private final SessionFactory sessionFactory;
    public AdminService() {
        super(Admin.class, HibernateAdminSessionFactory.getSessionFactory());
        this.sessionFactory = HibernateAdminSessionFactory.getSessionFactory();
    }

    public Admin getAdminByLogin(String login) {
        String queryHQL = "from Admin where adminLogin = :ADMIN_LOGIN";
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(queryHQL, Admin.class)
                    .setParameter("ADMIN_LOGIN", login.getBytes())
                    .uniqueResult();
        }
    }

    public Admin authenticationVerification(String login, String password) {
        String queryHQL = "from Admin where adminLogin = :ADMIN_LOGIN and adminPassword = :ADMIN_PASSWORD";
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(queryHQL, Admin.class)
                    .setParameter("ADMIN_LOGIN", login.getBytes())
                    .setParameter("ADMIN_PASSWORD", password.getBytes())
                    .uniqueResult();
        }
    }
}
