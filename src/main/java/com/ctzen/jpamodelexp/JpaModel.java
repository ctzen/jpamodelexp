package com.ctzen.jpamodelexp;

import javax.annotation.Generated;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.StaticMetamodel;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * JPA static metamodel meta.
 *
 * @author cchang
 */
class JpaModel implements Comparable<JpaModel> {

    JpaModel(ManagedType<?> type) {
        jpaClass = type.getJavaType();
        packageName = jpaClass.getPackage() == null ? null : jpaClass.getPackage().getName();
        simpleName = jpaClass.getSimpleName() + "_";
        canonicalName = packageName + "." + simpleName;
        imports = new JpaModelImports(packageName, Generated.class, StaticMetamodel.class);
        type.getDeclaredAttributes().forEach(jpaAttr -> {
            JpaModelAttribute jma = new JpaModelAttribute(jpaAttr);
            attributes.add(jma);
            imports.add(jma.getJpaClass());
            imports.add(jma.getGenerics());
        });
    }

    private final Class<?> jpaClass;

    Class<?> getJpaClass() {
        return jpaClass;
    }

    boolean isModelOf(Class<?> jpaClass) {
        return this.jpaClass.equals(jpaClass);
    }

    private final String packageName;

    boolean hasPackage() {
        return packageName != null;
    }

    String getPackageName() {
        return packageName;
    }

    private final String simpleName;

    String getSimpleName() {
        return simpleName;
    }

    private final String canonicalName;

    String getCanonicalName() {
        return canonicalName;
    }

    private final JpaModelImports imports;

    JpaModelImports getImports() {
        return imports;
    }

    private JpaModel extendz;

    boolean hasExtendz() {
        return extendz != null;
    }

    JpaModel getExtendz() {
        return extendz;
    }

    void setExtendz(JpaModel extendz) {
        this.extendz = extendz;
    }

    private final Set<JpaModelAttribute> attributes = new TreeSet<>();

    boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    Stream<JpaModelAttribute> attributeStream() {
        return attributes.stream();
    }

    void finalizeModel() {
        imports.finalizeImports();
    }

    @Override
    public int hashCode() {
        return canonicalName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JpaModel && canonicalName.equals(((JpaModel)obj).canonicalName);
    }

    @Override
    public int compareTo(JpaModel o) {
        return canonicalName.compareTo(o.canonicalName);
    }

}
