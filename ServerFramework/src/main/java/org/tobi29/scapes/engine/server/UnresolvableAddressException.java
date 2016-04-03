package org.tobi29.scapes.engine.server;

import java.io.IOException;

public class UnresolvableAddressException extends IOException {
    public UnresolvableAddressException(String hostname) {
        super(hostname);
    }
}
