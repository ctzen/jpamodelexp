package jpamodelexp.test.mirror

import groovy.transform.CompileStatic

import javax.persistence.ElementCollection
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Version

/**
 * @author cchang
 */
@CompileStatic
@Entity
class TestEntity extends TestMappedSuperclass {

    @Id
    long id

    @Version
    private long ver

    long getVer() {
        return ver
    }

    String str

    int[] intArray

    @ElementCollection
    Set<String> strSet

    @ElementCollection
    List<Integer> intList

    @OneToMany
    Collection<TestAssociatedEntity> assocCollection

    @ElementCollection
    Map<Integer, String> intStrMap

    @Embedded
    TestEmbeddable embeddable

    // test conflicting imports
    java.sql.Date sqlDate
    Date utilDate

}
