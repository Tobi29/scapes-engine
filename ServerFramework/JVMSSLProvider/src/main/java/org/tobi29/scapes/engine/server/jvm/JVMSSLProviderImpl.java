package org.tobi29.scapes.engine.server.jvm;

import java8.util.Optional;
import java8.util.function.Predicate;
import org.tobi29.scapes.engine.server.SSLHandle;
import org.tobi29.scapes.engine.server.SSLProviderImpl;

import javax.net.ssl.KeyManager;
import java.io.IOException;
import java.security.cert.X509Certificate;

public class JVMSSLProviderImpl implements SSLProviderImpl {
    @Override
    public SSLHandle sslHandle(KeyManager[] keyManagers,
            Optional<Predicate<X509Certificate[]>> feedbackPredicate)
            throws IOException {
        return new JVMSSLHandle(keyManagers, feedbackPredicate);
    }
}
