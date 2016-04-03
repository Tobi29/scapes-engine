package org.tobi29.scapes.engine.server;

import java8.util.Optional;
import java8.util.function.Predicate;

import javax.net.ssl.KeyManager;
import java.io.IOException;
import java.security.cert.X509Certificate;

public interface SSLProviderImpl {
    SSLHandle sslHandle(KeyManager[] keyManagers,
            Optional<Predicate<X509Certificate[]>> feedbackPredicate)
            throws IOException;
}
