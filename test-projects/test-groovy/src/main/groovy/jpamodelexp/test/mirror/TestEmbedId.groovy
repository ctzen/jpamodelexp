package jpamodelexp.test.mirror

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

import javax.persistence.Embeddable

/**
 * @author cchang
 */
@CompileStatic
@Embeddable
@EqualsAndHashCode
class TestEmbedId implements Serializable {

    int id1

    int id2

}
