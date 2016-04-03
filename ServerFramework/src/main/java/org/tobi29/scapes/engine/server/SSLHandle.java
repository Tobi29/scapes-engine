package org.tobi29.scapes.engine.server;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.security.cert.X509Certificate;

public interface SSLHandle {
    SSLEngine newEngine(RemoteAddress address) throws IOException;

    boolean certificateFeedback(X509Certificate[] certificates);

    boolean requiresVerification();

    void verifySession(RemoteAddress address, SSLEngine engine,
            X509Certificate[] certificates) throws IOException;
}
