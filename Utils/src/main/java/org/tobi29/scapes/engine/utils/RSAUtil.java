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

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public final class RSAUtil {
    private static final KeyFactory FACTORY;

    static {
        try {
            FACTORY = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedJVMException("RSA-keys not supported");
        }
    }

    public static RSAPrivateCrtKey readPrivate(String str)
            throws InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(ArrayUtil.fromBase64(str));
        return (RSAPrivateCrtKey) FACTORY.generatePrivate(keySpec);
    }

    public static PublicKey extractPublic(RSAPrivateCrtKey key)
            throws InvalidKeySpecException {
        RSAPublicKeySpec publicKeySpec =
                new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());
        return FACTORY.generatePublic(publicKeySpec);
    }

    public static PublicKey readPublic(String str)
            throws InvalidKeySpecException {
        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(ArrayUtil.fromBase64(str));
        return FACTORY.generatePublic(spec);
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
}
