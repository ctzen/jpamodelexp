package com.ctzen.jpamodelexp.jpa;

import com.ctzen.jpamodelexp.JpaMetamodel;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link JpaMetamodel} builder via annotated JPA classes.
 *
 * @author cchang
 */
public class AnnotatedClassesJpaMetamodelBuilder extends AbstractJpaMetamodelBuilder {

    public AnnotatedClassesJpaMetamodelBuilder() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public AnnotatedClassesJpaMetamodelBuilder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private final ClassLoader classLoader;

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className, true, classLoader);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private final Set<Class<?>> annotatedClasses = new HashSet<>();

    public void addAnnotatedClasses(Collection<Class<?>> classes) {
        annotatedClasses.addAll(classes);
    }

    public void addAnnotatedClasses(Class<?>... classes) {
        addAnnotatedClasses(Arrays.asList(classes));
    }

    private void addAnnotatedClassNames(Stream<String> classNames) {
        addAnnotatedClasses(classNames.map(this::loadClass).collect(Collectors.toSet()));
    }

    public void addAnnotatedClassNames(Collection<String> classNames) {
        addAnnotatedClassNames(classNames.stream());
    }

    public void addAnnotatedClassNames(String... classNames) {
        addAnnotatedClassNames(Arrays.stream(classNames));
    }

    public JpaMetamodel build() {
        Thread.currentThread().setContextClassLoader(classLoader);
        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DATASOURCE, getDataSource())
                .applySetting(AvailableSettings.DIALECT, DIALECT)
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "none")
                .applySetting(AvailableSettings.JPA_VALIDATION_MODE, "none")
                .build();
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        annotatedClasses.forEach(metadataSources::addAnnotatedClass);
        Metadata metadata = metadataSources.getMetadataBuilder().build();
        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();
        return new SessionFactoryMetamodel(sessionFactory);
    }

}
