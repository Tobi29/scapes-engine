/*
 * Copyright 2012-2015 Tobi29
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
package org.tobi29.scapes.engine.utils;

import org.tobi29.scapes.engine.utils.io.ByteStreamInputStream;
import org.tobi29.scapes.engine.utils.io.IOBiConsumer;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.regex.Pattern;

public final class SSLUtil {
    private static final KeyFactory FACTORY;
    // TODO: Need better key storage to be more future proof
    private static final BigInteger E = new BigInteger("65537");
    private static final Pattern SPLIT_COMMA = Pattern.compile(",");
    private static final Pattern SPLIT_EQUALS = Pattern.compile("=");

    static {
        try {
            FACTORY = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedJVMException("RSA-keys not supported");
        }
    }

    public static RSAPrivateKey readPrivate(String str)
            throws InvalidKeySpecException {
        try {
            PKCS8EncodedKeySpec keySpec =
                    new PKCS8EncodedKeySpec(ArrayUtil.fromBase64(str));
            return (RSAPrivateKey) FACTORY.generatePrivate(keySpec);
        } catch (IOException e) {
            throw new InvalidKeySpecException(e);
        }
    }

    public static PublicKey extractPublic(RSAPrivateKey key)
            throws InvalidKeySpecException {
        RSAPublicKeySpec publicKeySpec =
                new RSAPublicKeySpec(key.getModulus(), E);
        return FACTORY.generatePublic(publicKeySpec);
    }

    public static PublicKey readPublic(String str)
            throws InvalidKeySpecException {
        try {
            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(ArrayUtil.fromBase64(str));
            return FACTORY.generatePublic(spec);
        } catch (IOException e) {
            throw new InvalidKeySpecException(e);
        }
    }

    public static String writePrivate(PrivateKey key)
            throws InvalidKeySpecException {
        PKCS8EncodedKeySpec spec =
                FACTORY.getKeySpec(key, PKCS8EncodedKeySpec.class);
        byte[] packed = spec.getEncoded();
        String key64 = ArrayUtil.toBase64(packed);
        Arrays.fill(packed, (byte) 0);
        return key64;
    }

    public static String writePublic(PublicKey key)
            throws InvalidKeySpecException {
        X509EncodedKeySpec spec =
                FACTORY.getKeySpec(key, X509EncodedKeySpec.class);
        return ArrayUtil.toBase64(spec.getEncoded());
    }

    public static KeyManager[] keyManagers(KeyStore keyStore, String password)
            throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        return keyManagerFactory.getKeyManagers();
    }

    public static TrustManager[] trustManagers(KeyStore keyStore)
            throws KeyStoreException, NoSuchAlgorithmException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return trustManagerFactory.getTrustManagers();
    }

    public static KeyStore keyStore(String filepath, String password,
            ClassLoader classLoader) throws IOException {
        try (InputStream streamIn = classLoader.getResourceAsStream(filepath)) {
            return keyStore(streamIn, password);
        }
    }

    public static KeyStore keyStore(ReadableByteStream stream, String password)
            throws IOException {
        try (InputStream streamIn = new ByteStreamInputStream(stream)) {
            return keyStore(streamIn, password);
        }
    }

    public static KeyStore keyStore(InputStream streamIn, String password)
            throws IOException {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(streamIn, password.toCharArray());
            return keyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new IOException(e);
        }
    }

    public static List<RSAPrivateKey> readPrivateKeys(BufferedReader reader)
            throws IOException {
        List<RSAPrivateKey> keys = new ArrayList<>();
        readPEM(reader, (type, data) -> {
            if (type != PEMType.PRIVATE_KEY) {
                return;
            }
            try {
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(data);
                keys.add((RSAPrivateKey) FACTORY.generatePrivate(keySpec));
            } catch (InvalidKeySpecException e) {
                throw new IOException(e);
            }
        });
        return keys;
    }

    public static List<Certificate> readCertificateChain(BufferedReader reader)
            throws IOException {
        try {
            CertificateFactory factory =
                    CertificateFactory.getInstance("X.509");
            List<Certificate> certificates = new ArrayList<>();
            readPEM(reader, (type, data) -> {
                if (type != PEMType.CERTIFICATE) {
                    return;
                }
                try {
                    certificates.add(factory.generateCertificate(
                            new ByteArrayInputStream(data)));
                } catch (CertificateException e) {
                    throw new IOException(e);
                }
            });
            return certificates;
        } catch (CertificateException e) {
            throw new IOException(e);
        }
    }

    public static void readPEM(BufferedReader reader,
            IOBiConsumer<PEMType, byte[]> consumer) throws IOException {
        String line = reader.readLine();
        PEMState state = PEMState.NONE;
        StringBuilder base64 = new StringBuilder(256);
        while (line != null) {
            switch (line) {
                case "-----BEGIN CERTIFICATE-----":
                    if (state != PEMState.NONE) {
                        throw new IOException(
                                "BEGIN CERTIFICATE inside of other section");
                    }
                    state = PEMState.CERTIFICATE;
                    break;
                case "-----END CERTIFICATE-----":
                    if (state != PEMState.CERTIFICATE) {
                        throw new IOException(
                                "END CERTIFICATE outside of section");
                    }
                    consumer.accept(PEMType.CERTIFICATE, decode(base64));
                    state = PEMState.NONE;
                    break;
                case "-----BEGIN PRIVATE KEY-----":
                    if (state != PEMState.NONE) {
                        throw new IOException(
                                "BEGIN PRIVATE KEY inside of other section");
                    }
                    state = PEMState.PRIVATE_KEY;
                    break;
                case "-----END PRIVATE KEY-----":
                    if (state != PEMState.PRIVATE_KEY) {
                        throw new IOException(
                                "END PRIVATE KEY outside of section");
                    }
                    consumer.accept(PEMType.PRIVATE_KEY, decode(base64));
                    state = PEMState.NONE;
                    break;
                default:
                    base64.append(line);
                    break;
            }
            line = reader.readLine();
        }
    }

    public static Map<String, String> parseX500(X500Principal principal) {
        Map<String, String> map = new HashMap<>();
        String[] mappings =
                SPLIT_COMMA.split(principal.getName(X500Principal.RFC2253));
        for (String mapping : mappings) {
            String[] split = SPLIT_EQUALS.split(mapping);
            if (split.length != 2) {
                // Let's just hope it never comes to this
                throw new IllegalArgumentException(
                        "X500Principal implementation violates RFC2253");
            }
            map.put(split[0], split[1]);
        }
        return Collections.unmodifiableMap(map);
    }

    private static byte[] decode(StringBuilder base64) throws IOException {
        byte[] array = ArrayUtil.fromBase64(base64.toString());
        base64.setLength(0);
        return array;
    }

    private enum PEMType {
        CERTIFICATE,
        PRIVATE_KEY
    }

    private enum PEMState {
        NONE,
        CERTIFICATE,
        PRIVATE_KEY
    }
}
