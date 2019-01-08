package jpamodelexp.test.mirror

import groovy.transform.CompileStatic

import javax.persistence.MappedSuperclass

/**
 * @author cchang
 */
@CompileStatic
@MappedSuperclass
abstract class TestMappedSuperclass {

    int superInt

}
