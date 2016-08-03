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

package org.tobi29.scapes.engine.server.jvm;

import java8.util.Optional;
import java8.util.function.Predicate;
import org.tobi29.scapes.engine.server.RemoteAddress;
import org.tobi29.scapes.engine.server.SSLHandle;
import org.tobi29.scapes.engine.utils.Streams;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;

public class JVMSSLHandle implements SSLHandle {
    private final SSLContext context;
    private final Predicate<X509Certificate[]> feedbackPredicate;

    public JVMSSLHandle(KeyManager[] keyManagers,
            Optional<Predicate<X509Certificate[]>> feedbackPredicate)
            throws IOException {
        try {
            context = SSLContext.getInstance("TLSv1.2");
            if (feedbackPredicate.isPresent()) {
                context.init(keyManagers,
                        trustManagers(feedbackPredicate.get()),
                        new SecureRandom());
                this.feedbackPredicate = feedbackPredicate.get();
            } else {
                context.init(keyManagers, null, new SecureRandom());
                this.feedbackPredicate = certificates -> false;
            }
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException(e);
        }
    }

    private static TrustManager[] trustManagers(
            Predicate<X509Certificate[]> feedbackPredicate) throws IOException {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            X509ExtendedTrustManager[] trustManagers =
                    Streams.of(trustManagerFactory.getTrustManagers())
                            .filter(manager -> manager instanceof X509ExtendedTrustManager)
                            .map(manager -> (X509ExtendedTrustManager) manager)
                            .toArray(X509ExtendedTrustManager[]::new);
            return new TrustManager[]{
                    new FeedbackExtendedTrustManager(trustManagers,
                            feedbackPredicate)};
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IOException(e);
        }
    }

    @Override
    public SSLEngine newEngine(RemoteAddress address) throws IOException {
        SSLEngine engine =
                context.createSSLEngine(address.address, address.port);
        SSLParameters parameters = context.getDefaultSSLParameters();
        parameters.setEndpointIdentificationAlgorithm("HTTPS");
        engine.setSSLParameters(parameters);
        return engine;
    }

    @Override
    public boolean certificateFeedback(X509Certificate[] certificates) {
        return feedbackPredicate.test(certificates);
    }

    @Override
    public boolean requiresVerification() {
        return false;
    }

    @Override
    public void verifySession(RemoteAddress address, SSLEngine engine,
            X509Certificate[] certificates)
            throws IOException {
        throw new UnsupportedOperationException(
                "Handle does not require verification");
    }
}
