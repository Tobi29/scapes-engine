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

package org.tobi29.scapes.engine.utils

import org.tobi29.scapes.engine.utils.io.ByteStreamInputStream
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.regex.Pattern
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.security.auth.x500.X500Principal

private val FACTORY: KeyFactory = KeyFactory.getInstance("RSA")
// TODO: Need better key storage to be more future proof
private val E = BigInteger("65537")
private val SPLIT_COMMA = Pattern.compile(",")
private val SPLIT_EQUALS = Pattern.compile("=")

/**
 * Reads a private RSA key, encoded in Base64
 * @param str String containing the PKCS8 encoded key in Base64
 * @return The [RSAPrivateKey]
 */
fun readPrivate(str: String): RSAPrivateKey {
    try {
        val keySpec = PKCS8EncodedKeySpec(str.fromBase64())
        return FACTORY.generatePrivate(keySpec) as RSAPrivateKey
    } catch (e: IOException) {
        throw InvalidKeySpecException(e)
    }

}

/**
 * Extracts the [PublicKey] from a [RSAPrivateKey]
 *
 * Note: This assumes the value `65537` of `E`
 * @param key The private key
 * @return The [PublicKey]
 */
fun extractPublic(key: RSAPrivateKey): PublicKey {
    val publicKeySpec = RSAPublicKeySpec(key.modulus, E)
    return FACTORY.generatePublic(publicKeySpec)
}

@Throws(InvalidKeySpecException::class)
fun readPublic(str: String): PublicKey {
    try {
        val spec = X509EncodedKeySpec(str.fromBase64())
        return FACTORY.generatePublic(spec)
    } catch (e: IOException) {
        throw InvalidKeySpecException(e)
    }

}

@Throws(InvalidKeySpecException::class)
fun writePrivate(key: PrivateKey): String {
    val spec = FACTORY.getKeySpec(key, PKCS8EncodedKeySpec::class.java)
    val packed = spec.encoded
    val key64 = packed.toBase64()
    Arrays.fill(packed, 0.toByte())
    return key64
}

@Throws(InvalidKeySpecException::class)
fun writePublic(key: PublicKey): String {
    val spec = FACTORY.getKeySpec(key, X509EncodedKeySpec::class.java)
    return spec.encoded.toBase64()
}

@Throws(KeyStoreException::class, NoSuchAlgorithmException::class,
        UnrecoverableKeyException::class)
fun keyManagers(keyStore: KeyStore,
                password: String): Array<KeyManager> {
    val keyManagerFactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm())
    keyManagerFactory.init(keyStore, password.toCharArray())
    return keyManagerFactory.keyManagers
}

@Throws(KeyStoreException::class, NoSuchAlgorithmException::class)
fun trustManagers(keyStore: KeyStore): Array<TrustManager> {
    val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(keyStore)
    return trustManagerFactory.trustManagers
}

@Throws(IOException::class)
fun keyStore(filepath: String,
             password: String,
             classLoader: ClassLoader): KeyStore {
    classLoader.getResourceAsStream(filepath).use { streamIn ->
        return keyStore(streamIn, password)
    }
}

@Throws(IOException::class)
fun keyStore(stream: ReadableByteStream,
             password: String): KeyStore {
    ByteStreamInputStream(stream).use { streamIn ->
        return keyStore(streamIn, password)
    }
}

@Throws(IOException::class)
fun keyStore(streamIn: InputStream,
             password: String): KeyStore {
    try {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(streamIn, password.toCharArray())
        return keyStore
    } catch (e: KeyStoreException) {
        throw IOException(e)
    } catch (e: NoSuchAlgorithmException) {
        throw IOException(e)
    } catch (e: CertificateException) {
        throw IOException(e)
    }

}

@Throws(IOException::class)
fun readPrivateKeys(reader: BufferedReader): List<RSAPrivateKey> {
    val keys = ArrayList<RSAPrivateKey>()
    readPEM(reader, { type, data ->
        if (type != PEMType.PRIVATE_KEY) {
            return@readPEM
        }
        try {
            val keySpec = PKCS8EncodedKeySpec(data)
            keys.add(FACTORY.generatePrivate(keySpec) as RSAPrivateKey)
        } catch (e: InvalidKeySpecException) {
            throw IOException(e)
        }
    })
    return keys
}

@Throws(IOException::class)
fun readCertificateChain(reader: BufferedReader): List<Certificate> {
    try {
        val factory = CertificateFactory.getInstance("X.509")
        val certificates = ArrayList<Certificate>()
        readPEM(reader, { type, data ->
            if (type != PEMType.CERTIFICATE) {
                return@readPEM
            }
            try {
                certificates.add(factory.generateCertificate(
                        ByteArrayInputStream(data)))
            } catch (e: CertificateException) {
                throw IOException(e)
            }
        })
        return certificates
    } catch (e: CertificateException) {
        throw IOException(e)
    }

}

@Throws(IOException::class)
fun readPEM(reader: BufferedReader,
            consumer: (PEMType, ByteArray) -> Unit) {
    var line: String? = reader.readLine()
    var state = PEMState.NONE
    val base64 = StringBuilder(256)
    while (line != null) {
        when (line) {
            "-----BEGIN CERTIFICATE-----" -> {
                if (state != PEMState.NONE) {
                    throw IOException(
                            "BEGIN CERTIFICATE inside of other section")
                }
                state = PEMState.CERTIFICATE
            }
            "-----END CERTIFICATE-----" -> {
                if (state != PEMState.CERTIFICATE) {
                    throw IOException(
                            "END CERTIFICATE outside of section")
                }
                consumer(PEMType.CERTIFICATE, decode(base64))
                state = PEMState.NONE
            }
            "-----BEGIN PRIVATE KEY-----" -> {
                if (state != PEMState.NONE) {
                    throw IOException(
                            "BEGIN PRIVATE KEY inside of other section")
                }
                state = PEMState.PRIVATE_KEY
            }
            "-----END PRIVATE KEY-----" -> {
                if (state != PEMState.PRIVATE_KEY) {
                    throw IOException(
                            "END PRIVATE KEY outside of section")
                }
                consumer(PEMType.PRIVATE_KEY, decode(base64))
                state = PEMState.NONE
            }
            else -> base64.append(line)
        }
        line = reader.readLine()
    }
}

fun parseX500(principal: X500Principal): Map<String, String> {
    val map = HashMap<String, String>()
    val mappings = SPLIT_COMMA.split(
            principal.getName(X500Principal.RFC2253))
    for (mapping in mappings) {
        val split = SPLIT_EQUALS.split(mapping)
        if (split.size != 2) {
            // Let's just hope it never comes to this
            throw IllegalArgumentException(
                    "X500Principal implementation violates RFC2253")
        }
        map.put(split[0], split[1])
    }
    return Collections.unmodifiableMap(map)
}

@Throws(IOException::class)
private fun decode(base64: StringBuilder): ByteArray {
    val array = base64.toString().fromBase64()
    base64.setLength(0)
    return array
}

enum class PEMType {
    CERTIFICATE,
    PRIVATE_KEY
}

enum class PEMState {
    NONE,
    CERTIFICATE,
    PRIVATE_KEY
}
