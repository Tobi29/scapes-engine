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
package org.tobi29.scapes.engine.server.jvm

import org.tobi29.scapes.engine.server.RemoteAddress
import org.tobi29.scapes.engine.server.SSLHandle
import org.tobi29.scapes.engine.utils.filterMap
import org.tobi29.scapes.engine.utils.toArray
import java.io.IOException
import java.security.*
import java.security.cert.X509Certificate
import javax.net.ssl.*

class JVMSSLHandle(keyManagers: Array<KeyManager>?,
                   feedbackPredicate: ((Array<X509Certificate>) -> Boolean)?) : SSLHandle {
    private val context: SSLContext
    private val feedbackPredicate: (Array<X509Certificate>) -> Boolean

    init {
        try {
            context = SSLContext.getInstance("TLSv1.2")
            if (feedbackPredicate != null) {
                context.init(keyManagers, trustManagers(feedbackPredicate),
                        SecureRandom())
                this.feedbackPredicate = feedbackPredicate
            } else {
                context.init(keyManagers, null, SecureRandom())
                this.feedbackPredicate = { certificates -> false }
            }
        } catch (e: NoSuchAlgorithmException) {
            throw IOException(e)
        } catch (e: KeyManagementException) {
            throw IOException(e)
        }
    }

    private fun trustManagers(
            feedbackPredicate: (Array<X509Certificate>) -> Boolean): Array<TrustManager> {
        try {
            val trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers.asSequence()
                    .filterMap<X509ExtendedTrustManager>().toArray()
            return arrayOf(FeedbackExtendedTrustManager(trustManagers,
                    feedbackPredicate))
        } catch (e: NoSuchAlgorithmException) {
            throw IOException(e)
        } catch (e: KeyStoreException) {
            throw IOException(e)
        }
    }

    override fun newEngine(address: RemoteAddress): SSLEngine {
        val engine = context.createSSLEngine(address.address,
                address.port)
        val parameters = context.defaultSSLParameters
        parameters.endpointIdentificationAlgorithm = "HTTPS"
        engine.sslParameters = parameters
        return engine
    }

    override fun certificateFeedback(certificates: Array<X509Certificate>): Boolean {
        return feedbackPredicate(certificates)
    }

    override fun requiresVerification(): Boolean {
        return false
    }

    override fun verifySession(address: RemoteAddress,
                               engine: SSLEngine,
                               certificates: Array<X509Certificate>) {
        throw UnsupportedOperationException(
                "Handle does not require verification")
    }
}
