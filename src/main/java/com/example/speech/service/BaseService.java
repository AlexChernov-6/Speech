package com.example.speech.service;

import com.example.speech.util.HibernateSessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class BaseService<T> {
    private final SessionFactory sessionFactory;
    private final Class<T> tClass;

    public BaseService(Class<T> tClass, SessionFactory sessionFactory) {
        this.tClass = tClass;
        this.sessionFactory = sessionFactory;
    }

    public BaseService(Class<T> tClass) {
        this.tClass = tClass;
        this.sessionFactory = HibernateSessionFactory.getSessionFactory();
    }

    public T getRowById(Long id) {
        try (Session session = sessionFactory.openSession()){
            return session.get(tClass, id);
        }
    }

    public List<T> getAllRow() {
        try (Session session = sessionFactory.openSession()){
            return session.createQuery("from " + tClass.getSimpleName(), tClass).list();
        }
    }

    public boolean save(T entity) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            return true;
        } catch (HibernateException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(T entity) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
            return true;
        } catch (HibernateException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean update(T entity) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.update(entity);
            transaction.commit();
            return true;
        } catch (HibernateException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}
