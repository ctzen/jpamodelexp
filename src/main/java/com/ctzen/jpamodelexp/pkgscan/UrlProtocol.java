package com.ctzen.jpamodelexp.pkgscan;

import java.net.URL;
import java.util.Arrays;

/**
 * Constantize {@link URL#getProtocol()}, not all possible protocols are defined.
 *
 * @author cchang
 */
public enum UrlProtocol {

    FILE("file"),
    JAR("jar")

    ;

    UrlProtocol(String protocol) {
        this.protocol = protocol;
    }

    private final String protocol;

    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocol  from URL.getProtocol()
     * @return corresponding UrlProtocol enum or null if unmapped
     */
    static UrlProtocol decode(String protocol) {
        return Arrays.stream(UrlProtocol.values())
                .filter(up -> up.getProtocol().equals(protocol))
                .findAny().orElse(null);
    }

}
