package org.tobi29.scapes.engine.server.spi;

import org.tobi29.scapes.engine.server.SSLProviderImpl;

public interface SSLProviderProvider {
    boolean available();

    SSLProviderImpl implementation();
}
