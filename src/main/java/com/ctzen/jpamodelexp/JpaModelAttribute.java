package com.ctzen.jpamodelexp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;

import javax.persistence.metamodel.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static javax.persistence.metamodel.PluralAttribute.CollectionType;

/**
 * JPA attribute meta.
 *
 * @author cchang
 */
class JpaModelAttribute implements Comparable<JpaModelAttribute> {

    private static Class<?> wrap(Class<?> clz) {
        return clz.isPrimitive() ? Primitives.wrap(clz) : clz;
    }

    private static final Map<CollectionType, Class<? extends Attribute>> COLLECTION_TYPE_ATTRIBUTE_MAP = ImmutableMap.of(
            CollectionType.COLLECTION, CollectionAttribute.class,
            CollectionType.SET, SetAttribute.class,
            CollectionType.LIST, ListAttribute.class,
            CollectionType.MAP, MapAttribute.class
    );

    JpaModelAttribute(Attribute<?, ?> jpaAttr) {
        name = jpaAttr.getName();
        if (jpaAttr instanceof SingularAttribute) {
            jpaClass = SingularAttribute.class;
            generics = ImmutableList.of(
                    jpaAttr.getDeclaringType().getJavaType(),
                    wrap(jpaAttr.getJavaType())
            );
        }
        else if (jpaAttr instanceof PluralAttribute) {
            PluralAttribute<?, ?, ?> pluralAttr = (PluralAttribute<?, ?, ?>)jpaAttr;
            jpaClass = COLLECTION_TYPE_ATTRIBUTE_MAP.get(pluralAttr.getCollectionType());
            if (jpaClass == null) {
                throw new IllegalArgumentException("Unhandled collection type: " + jpaAttr);
            }
            if (pluralAttr instanceof MapAttribute) {
                MapAttribute<?, ?, ?> mapAttr = (MapAttribute)pluralAttr;
                generics = ImmutableList.of(
                        mapAttr.getDeclaringType().getJavaType(),
                        mapAttr.getKeyType().getJavaType(),
                        mapAttr.getElementType().getJavaType()
                );
            }
            else {
                generics = ImmutableList.of(
                        pluralAttr.getDeclaringType().getJavaType(),
                        pluralAttr.getElementType().getJavaType()
                );
            }
        }
        else {
            throw new IllegalArgumentException("Unhandled: " + jpaAttr);
        }
    }

    private final String name;

    String getName() {
        return name;
    }

    String getConstName() {
        // as per jpamodelgen's StringUtil.getUpperUnderscoreCaseFromLowerCamelCase()
        return name.replaceAll("(.)(\\p{Upper})", "$1_$2").toUpperCase();
    }

    private final Class<? extends Attribute> jpaClass;

    Class<? extends Attribute> getJpaClass() {
        return jpaClass;
    }

    private final List<Class<?>> generics;

    boolean hasGenerics() {
        return !generics.isEmpty();
    }

    List<Class<?>> getGenerics() {
        return generics;
    }

    Stream<Class<?>> genericsStream() {
        return generics.stream();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JpaModelAttribute && name.equals(((JpaModelAttribute)obj).name);
    }

    @Override
    public int compareTo(JpaModelAttribute o) {
        return name.compareTo(o.name);
    }

}
