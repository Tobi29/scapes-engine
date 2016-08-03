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

import java8.util.function.Predicate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedbackExtendedTrustManager extends X509ExtendedTrustManager {
    private final X509ExtendedTrustManager[] trustManagers;
    private final Predicate<X509Certificate[]> feedbackPredicate;

    public FeedbackExtendedTrustManager(
            X509ExtendedTrustManager[] trustManagers,
            Predicate<X509Certificate[]> feedbackPredicate) {
        this.trustManagers = trustManagers;
        this.feedbackPredicate = feedbackPredicate;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s,
            Socket socket) throws CertificateException {
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkClientTrusted(x509Certificates, s, socket);
            }
        } catch (CertificateException e) {
            if (!feedbackPredicate.test(x509Certificates)) {
                throw e;
            }
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s,
            Socket socket) throws CertificateException {
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkServerTrusted(x509Certificates, s, socket);
            }
        } catch (CertificateException e) {
            if (!feedbackPredicate.test(x509Certificates)) {
                throw e;
            }
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s,
            SSLEngine sslEngine) throws CertificateException {
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkClientTrusted(x509Certificates, s, sslEngine);
            }
        } catch (CertificateException e) {
            if (!feedbackPredicate.test(x509Certificates)) {
                throw e;
            }
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s,
            SSLEngine sslEngine) throws CertificateException {
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkServerTrusted(x509Certificates, s, sslEngine);
            }
        } catch (CertificateException e) {
            if (!feedbackPredicate.test(x509Certificates)) {
                throw e;
            }
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkClientTrusted(x509Certificates, s);
            }
        } catch (CertificateException e) {
            if (!feedbackPredicate.test(x509Certificates)) {
                throw e;
            }
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
        try {
            for (X509ExtendedTrustManager trustManager : trustManagers) {
                trustManager.checkServerTrusted(x509Certificates, s);
            }
        } catch (CertificateException e) {
            if (!feedbackPredicate.test(x509Certificates)) {
                throw e;
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        List<X509Certificate> issuers = new ArrayList<>();
        for (X509ExtendedTrustManager trustManager : trustManagers) {
            Collections.addAll(issuers, trustManager.getAcceptedIssuers());
        }
        return issuers.toArray(new X509Certificate[issuers.size()]);
    }
}
