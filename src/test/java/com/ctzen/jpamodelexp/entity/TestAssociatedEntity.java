package com.ctzen.jpamodelexp.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author cchang
 */
@Entity
public class TestAssociatedEntity {

    @EmbeddedId
    private TestEmbedId id;

    @ManyToOne
    private TestEntity owner;

}
