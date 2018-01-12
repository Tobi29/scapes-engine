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

import org.tobi29.scapes.engine.utils.io.ByteChannel
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.io.ReadableByteChannel
import org.tobi29.scapes.engine.utils.io.WritableByteChannel
import org.tobi29.scapes.engine.utils.isAndroidAPI
import org.tobi29.scapes.engine.utils.toArray
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import kotlin.coroutines.experimental.CoroutineContext

class SSLHandle(keyManagers: Array<KeyManager>?,
                trustManagers: Array<TrustManager>?,
                private val verifyHostname: Boolean = true) {
    private val context: SSLContext = if (isAndroidAPI(20)) {
        SSLContext.getInstance("TLSv1.2")
    } else {
        SSLContext.getInstance("TLSv1")
    }

    init {
        if (trustManagers == null) {
            context.init(keyManagers, trustManagers(), SecureRandom())
        } else {
            context.init(keyManagers, trustManagers, SecureRandom())
        }
    }

    constructor(verifyHostname: Boolean = true
    ) : this(null, null, verifyHostname)

    constructor(keyManagers: Array<KeyManager>,
                verifyHostname: Boolean = true
    ) : this(keyManagers, null, verifyHostname)

    constructor(trustManagers: Array<TrustManager>,
                verifyHostname: Boolean = true
    ) : this(null, trustManagers, verifyHostname)

    private fun trustManagers(): Array<TrustManager> {
        try {
            val trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            return trustManagerFactory.trustManagers.asSequence()
                    .map(trustManagerWrap).toArray()
        } catch (e: NoSuchAlgorithmException) {
            throw IOException(e)
        } catch (e: KeyStoreException) {
            throw IOException(e)
        }
    }

    fun newSSLChannel(address: RemoteAddress,
                      channel: ByteChannel,
                      taskExecutor: CoroutineContext,
                      client: Boolean) =
            newSSLChannel(address, channel, channel, taskExecutor, client)

    fun newSSLChannel(address: RemoteAddress,
                      channelRead: ReadableByteChannel,
                      channelWrite: WritableByteChannel,
                      taskExecutor: CoroutineContext,
                      client: Boolean): SSLChannel {
        val engine = newEngine(address).apply {
            needClientAuth = client
            useClientMode = client
            beginHandshake()
        }
        return SSLChannel(address, channelRead, channelWrite, taskExecutor,
                this, engine)
    }

    fun newEngine(address: RemoteAddress): SSLEngine {
        val engine = context.createSSLEngine(address.address,
                address.port)
        val parameters = context.defaultSSLParameters
        if (verifyHostname && isAndroidAPI(24)) {
            parameters::class.java
                    .getMethod("setEndpointIdentificationAlgorithm",
                            String::class.java)
                    .invoke(parameters, "HTTPS")
        }
        engine.sslParameters = parameters
        return engine
    }

    /**
     * @throws IOException
     */
    fun verifySession(address: RemoteAddress,
                      session: SSLSession,
                      client: Boolean) {
        try {
            if (client && verifyHostname && !isAndroidAPI(24)) {
                val verifier = HttpsURLConnection.getDefaultHostnameVerifier()
                if (!verifier.verify(address.address, session)) {
                    throw SSLHandshakeException("Hostname verification failed")
                }
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    companion object {
        fun fromCertificates(certificates: Array<X509Certificate>,
                             verifyHostname: Boolean = false) =
                SSLHandle(arrayOf<TrustManager>(
                        CertificateX509TrustManager(certificates)),
                        verifyHostname)

        fun insecure(verifyHostname: Boolean = false) =
                SSLHandle(arrayOf<TrustManager>(DummyX509TrustManager),
                        verifyHostname)
    }
}

private val trustManagerWrap: (TrustManager) -> TrustManager =
        if (isAndroidAPI(24)) {
            { trustManager ->
                when (trustManager) {
                    is X509ExtendedTrustManager ->
                        SavingExtendedTrustManager(trustManager)
                    is X509TrustManager ->
                        SavingTrustManager(trustManager)
                    else -> trustManager
                }
            }
        } else {
            { trustManager ->
                when (trustManager) {
                    is X509TrustManager ->
                        SavingTrustManager(trustManager)
                    else -> trustManager
                }
            }
        }
