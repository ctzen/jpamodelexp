package com.ctzen.jpamodelexp.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author cchang
 */
@Embeddable
public class TestEmbedId implements Serializable {

    private int id1;

    private int id2;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
