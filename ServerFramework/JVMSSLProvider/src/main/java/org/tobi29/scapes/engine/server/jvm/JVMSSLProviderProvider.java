package org.tobi29.scapes.engine.server.jvm;

import org.tobi29.scapes.engine.server.SSLProviderImpl;
import org.tobi29.scapes.engine.server.spi.SSLProviderProvider;

public class JVMSSLProviderProvider implements SSLProviderProvider {
    @Override
    public boolean available() {
        return true;
    }

    @Override
    public SSLProviderImpl implementation() {
        return new JVMSSLProviderImpl();
    }
}
