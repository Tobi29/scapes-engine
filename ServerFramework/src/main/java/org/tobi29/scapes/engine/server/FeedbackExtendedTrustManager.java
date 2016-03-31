package org.tobi29.scapes.engine.server;

import java8.util.function.Predicate;
import org.tobi29.scapes.engine.utils.Streams;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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

    public static TrustManager[] defaultTrustManager(
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
