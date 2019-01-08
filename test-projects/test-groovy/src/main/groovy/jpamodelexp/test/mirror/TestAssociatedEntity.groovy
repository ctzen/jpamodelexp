package jpamodelexp.test.mirror

import groovy.transform.CompileStatic

import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.ManyToOne

/**
 * @author cchang
 */
@CompileStatic
@Entity
class TestAssociatedEntity {

    @EmbeddedId
    TestEmbedId id

    @ManyToOne
    TestEntity owner

}
