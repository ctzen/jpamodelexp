package com.ctzen.jpamodelexp.jpa;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.dialect.H2Dialect;

import javax.sql.DataSource;

/**
 * @author cchang
 */
public abstract class AbstractJpaMetamodelBuilder {

    private static DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUser("sa");
        dataSource.setPassword("");
        dataSource.setUrl("jdbc:h2:mem:jpamodelexp");
        return dataSource;
    }

    protected static final String DIALECT = H2Dialect.class.getCanonicalName();

    protected AbstractJpaMetamodelBuilder() {
        dataSource = createDataSource();
    }

    private final DataSource dataSource;

    protected DataSource getDataSource() {
        return dataSource;
    }

}
