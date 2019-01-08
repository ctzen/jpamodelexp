package com.ctzen.jpamodelexp.jpa;

import com.ctzen.jpamodelexp.JpaMetamodel;
import org.hibernate.SessionFactory;

import javax.persistence.metamodel.Metamodel;

/**
 * Supplies {@link Metamodel} via {@link SessionFactory}
 *
 * @author cchang
 */
public class SessionFactoryMetamodel implements JpaMetamodel {

    public SessionFactoryMetamodel(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private final SessionFactory sessionFactory;

    @Override
    public Metamodel getMetamodel() {
        return sessionFactory.getMetamodel();
    }

    @Override
    public void close() {
        sessionFactory.close();
    }

}
