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
package org.tobi29.scapes.engine.server.jvm

import java.net.Socket
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager

class FeedbackExtendedTrustManager(
        private val trustManagers: Array<X509ExtendedTrustManager>,
        private val feedbackPredicate: Function1<Array<X509Certificate>, Boolean>) : X509ExtendedTrustManager() {

    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String,
                                    socket: Socket) {
        try {
            for (trustManager in trustManagers) {
                trustManager.checkClientTrusted(x509Certificates, s, socket)
            }
        } catch (e: CertificateException) {
            if (!feedbackPredicate.invoke(x509Certificates)) {
                throw e
            }
        }
    }

    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String,
                                    socket: Socket) {
        try {
            for (trustManager in trustManagers) {
                trustManager.checkServerTrusted(x509Certificates, s, socket)
            }
        } catch (e: CertificateException) {
            if (!feedbackPredicate.invoke(x509Certificates)) {
                throw e
            }
        }
    }

    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String,
                                    sslEngine: SSLEngine) {
        try {
            for (trustManager in trustManagers) {
                trustManager.checkClientTrusted(x509Certificates, s, sslEngine)
            }
        } catch (e: CertificateException) {
            if (!feedbackPredicate.invoke(x509Certificates)) {
                throw e
            }
        }
    }

    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String,
                                    sslEngine: SSLEngine) {
        try {
            for (trustManager in trustManagers) {
                trustManager.checkServerTrusted(x509Certificates, s, sslEngine)
            }
        } catch (e: CertificateException) {
            if (!feedbackPredicate.invoke(x509Certificates)) {
                throw e
            }
        }
    }

    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String) {
        try {
            for (trustManager in trustManagers) {
                trustManager.checkClientTrusted(x509Certificates, s)
            }
        } catch (e: CertificateException) {
            if (!feedbackPredicate.invoke(x509Certificates)) {
                throw e
            }
        }
    }

    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String) {
        try {
            for (trustManager in trustManagers) {
                trustManager.checkServerTrusted(x509Certificates, s)
            }
        } catch (e: CertificateException) {
            if (!feedbackPredicate.invoke(x509Certificates)) {
                throw e
            }
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        val issuers = ArrayList<X509Certificate>()
        for (trustManager in trustManagers) {
            Collections.addAll(issuers, *trustManager.acceptedIssuers)
        }
        return issuers.toTypedArray()
    }
}
