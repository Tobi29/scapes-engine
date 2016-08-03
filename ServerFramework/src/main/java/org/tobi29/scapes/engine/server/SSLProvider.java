/*
 * Copyright 2012-2016 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.scapes.engine.server;

import java8.util.Optional;
import java8.util.function.Predicate;
import org.tobi29.scapes.engine.server.spi.SSLProviderProvider;
import org.tobi29.scapes.engine.utils.UnsupportedJVMException;

import javax.net.ssl.KeyManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class SSLProvider {
    private static final SSLProviderImpl IMPL = loadService();

    private static SSLProviderImpl loadService() {
        for (SSLProviderProvider trustManager : ServiceLoader
                .load(SSLProviderProvider.class)) {
            try {
                if (trustManager.available()) {
                    return trustManager.implementation();
                }
            } catch (ServiceConfigurationError e) {
            }
        }
        throw new UnsupportedJVMException(
                "No trust manager implementation available");
    }

    public static SSLHandle sslHandle() throws IOException {
        return sslHandle(null, Optional.empty());
    }

    public static SSLHandle sslHandle(KeyManager[] keyManagers)
            throws IOException {
        return sslHandle(keyManagers, Optional.empty());
    }

    public static SSLHandle sslHandle(
            Predicate<X509Certificate[]> feedbackPredicate) throws IOException {
        return sslHandle(null, feedbackPredicate);
    }

    public static SSLHandle sslHandle(KeyManager[] keyManagers,
            Predicate<X509Certificate[]> feedbackPredicate) throws IOException {
        return sslHandle(keyManagers, Optional.of(feedbackPredicate));
    }

    public static SSLHandle sslHandle(KeyManager[] keyManagers,
            Optional<Predicate<X509Certificate[]>> feedbackPredicate)
            throws IOException {
        return IMPL.sslHandle(keyManagers, feedbackPredicate);
    }
}
