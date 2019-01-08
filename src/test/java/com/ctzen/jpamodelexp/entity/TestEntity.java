package com.ctzen.jpamodelexp.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author cchang
 */
@Entity
public class TestEntity extends TestMappedSuperclass {

    @Id
    private long id;

    @Version
    private long ver;

    private String str;

    private int[] intArray;

    @ElementCollection
    private Set<String> strSet;

    @ElementCollection
    private List<Integer> intList;

    @OneToMany
    private Collection<TestAssociatedEntity> assocCollection;

    @ElementCollection
    private Map<Integer, String> intStrMap;

    @Embedded
    private TestEmbeddable embeddable;

    // test conflicting imports
    private java.sql.Date sqlDate;
    private java.util.Date utilDate;

}
