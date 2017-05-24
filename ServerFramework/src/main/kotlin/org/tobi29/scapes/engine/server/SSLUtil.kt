/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.server

import org.tobi29.scapes.engine.utils.*
import org.tobi29.scapes.engine.utils.io.ByteStreamInputStream
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.io.ReadableByteStream
import java.io.BufferedReader
import java.io.ByteArrayInputStream
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
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.security.auth.x500.X500Principal

private val FACTORY: KeyFactory = KeyFactory.getInstance("RSA")
// TODO: Need better key storage to be more future proof
private val e = BigInteger("65537")

/**
 * Reads a private RSA key, encoded in Base64
 *
 * Can be written using [writePrivate]
 * @param str [String] containing the PKCS8 encoded key in Base64
 * @throws InvalidKeySpecException when an invalid key was given
 * @return The [RSAPrivateKey]
 */
// TODO: @Throws(InvalidKeySpecException::class)
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
 * @throws InvalidKeySpecException when an invalid key was given
 * @return The [PublicKey]
 */
// TODO: @Throws(InvalidKeySpecException::class)
fun extractPublic(key: RSAPrivateKey): PublicKey {
    val publicKeySpec = RSAPublicKeySpec(key.modulus, e)
    return FACTORY.generatePublic(publicKeySpec)
}

/**
 * Reads a public RSA key, encoded in Base64
 *
 * Can be written using [writePublic]
 * @param str [String] containing the X509 encoded key in Base64
 * @throws InvalidKeySpecException when an invalid key was given
 * @return The [PublicKey]
 */
// TODO: @Throws(InvalidKeySpecException::class)
fun readPublic(str: String): PublicKey {
    try {
        val spec = X509EncodedKeySpec(str.fromBase64())
        return FACTORY.generatePublic(spec)
    } catch (e: IOException) {
        throw InvalidKeySpecException(e)
    }

}

/**
 * Writes a public key into a Base64 encoded [String]
 *
 * Can be read using [readPrivate]
 * @param key The private key to write
 * @throws InvalidKeySpecException  when an invalid key was given
 * @return A [String] containing the PKCS8 encoded key in Base64
 */
// TODO: @Throws(InvalidKeySpecException::class)
fun writePrivate(key: PrivateKey): String {
    val spec = FACTORY.getKeySpec(key, PKCS8EncodedKeySpec::class.java)
    val packed = spec.encoded
    val key64 = packed.toBase64()
    packed.fill { 0 }
    return key64
}

/**
 * Writes a public key into a Base64 encoded [String]
 *
 * Can be read using [readPublic]
 * @param key The public key to write
 * @throws InvalidKeySpecException  when an invalid key was given
 * @return A [String] containing the X509 encoded key in Base64
 */
// TODO: @Throws(InvalidKeySpecException::class)
fun writePublic(key: PublicKey): String {
    val spec = FACTORY.getKeySpec(key, X509EncodedKeySpec::class.java)
    return spec.encoded.toBase64()
}

/**
 * Creates an array of [KeyManager]s from the given [KeyStore]
 * @param keyStore The [KeyStore] to take the keys from
 * @param password Password of the key to use
 * @throws KeyStoreException  when an error occurred
 * @return An [Array] containing the [KeyManager]s created from the [KeyStore]
 */
// TODO: @Throws(KeyStoreException::class)
fun keyManagers(keyStore: KeyStore,
                password: String): Array<KeyManager> {
    try {
        val keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, password.toCharArray())
        return keyManagerFactory.keyManagers
    } catch(e: NoSuchAlgorithmException) {
        throw KeyStoreException(e)
    } catch(e: UnrecoverableKeyException) {
        throw KeyStoreException(e)
    }
}

/**
 * Creates an array of [TrustManager]s from the given [KeyStore]
 * @param keyStore The [KeyStore] to take the keys from
 * @throws KeyStoreException  when an error occurred
 * @return An [Array] containing the [TrustManager]s created from the [KeyStore]
 */
// TODO: @Throws(KeyStoreException::class)
fun trustManagers(keyStore: KeyStore): Array<TrustManager> {
    try {
        val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        return trustManagerFactory.trustManagers
    } catch(e: NoSuchAlgorithmException) {
        throw KeyStoreException(e)
    }
}

/**
 * Loads a [KeyStore] from the classpath
 * @param filepath Path to the resource
 * @param password Password of the [KeyStore]
 * @param classLoader [ClassLoader] to load the resource from
 * @throws IOException when an IO error occurs
 * @return A [KeyStore] containing the read keys
 */
// TODO: @Throws(IOException::class)
fun keyStore(filepath: String,
             password: String,
             classLoader: ClassLoader): KeyStore {
    classLoader.getResourceAsStream(filepath).use { streamIn ->
        return keyStore(streamIn, password)
    }
}

/**
 * Loads a [KeyStore] from the [ReadableByteStream]
 * @param stream Stream to read from
 * @param password Password of the [KeyStore]
 * @throws IOException when an IO error occurs
 * @return A [KeyStore] containing the read keys
 */
// TODO: @Throws(IOException::class)
fun keyStore(stream: ReadableByteStream,
             password: String): KeyStore {
    ByteStreamInputStream(stream).use { streamIn ->
        return keyStore(streamIn, password)
    }
}

/**
 * Loads a [KeyStore] from the [InputStream]
 * @param streamIn Stream to read from
 * @param password Password of the [KeyStore]
 * @throws IOException when an IO error occurs
 * @return A [KeyStore] containing the read keys
 */
// TODO: @Throws(IOException::class)
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

/**
 * Reads the private keys from a PEM file
 * @param reader [BufferedReader] to read the file from
 * @throws IOException when an IO error occurs
 * @return A list of [RSAPrivateKey]s
 */
// TODO: @Throws(IOException::class)
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

/**
 * Reads the certificates from a PEM file
 * @param reader [BufferedReader] to read the file from
 * @throws IOException when an IO error occurs
 * @return A list of [Certificate]s
 */
// TODO: @Throws(IOException::class)
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

/**
 * Reads the content from a PEM file
 * @param reader [BufferedReader] to read the file from
 * @param consumer Called for each entry in the file
 * @throws IOException when an IO error occurs
 */
// TODO: @Throws(IOException::class)
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

/**
 * Parses the content of the given [X500Principal] into a map for easy access
 * @param principal The principal to parse
 * @return A map containing key-value pair from the entries in the principal
 */
fun parseX500(principal: X500Principal): Map<String, String> {
    val map = HashMap<String, String>()
    val mappings = principal.getName(X500Principal.RFC2253).split(',')
    for (mapping in mappings) {
        val split = mapping.split('=', limit = 2)
        if (split.size != 2) {
            // Let's just hope it never comes to this
            throw IllegalArgumentException(
                    "X500Principal implementation violates RFC2253")
        }
        map.put(split[0], split[1])
    }
    return map.readOnly()
}

/**
 * Type of an entry in a PEM file
 */
enum class PEMType {
    /**
     * Certificate entry
     */
    CERTIFICATE,
    /**
     * Private key entry
     */
    PRIVATE_KEY
}

private fun decode(base64: StringBuilder): ByteArray {
    val array = base64.toString().fromBase64()
    base64.setLength(0)
    return array
}

private enum class PEMState {
    NONE,
    CERTIFICATE,
    PRIVATE_KEY
}
