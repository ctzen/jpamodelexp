package com.ctzen.jpamodelexp;

import javax.persistence.metamodel.Metamodel;
import java.io.Closeable;

/**
 * {@link Metamodel} supplier.
 *
 * @author cchang
 */
public interface JpaMetamodel extends Closeable {

    Metamodel getMetamodel();

    @Override   // hides the checked IOException
    void close();

}
