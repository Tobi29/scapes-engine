/*
 * Copyright 2012-2018 Tobi29
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
package org.tobi29.server

import java.net.Socket
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager
import javax.net.ssl.X509TrustManager

class SavingTrustManager(
        private val trustManager: X509TrustManager
) : X509TrustManager {
    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String) {
        try {
            trustManager.checkClientTrusted(x509Certificates, s)
        } catch (e: CertificateException) {
            throw SavedCertificateException(e, x509Certificates)
        }
    }

    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String) {
        try {
            trustManager.checkServerTrusted(x509Certificates, s)
        } catch (e: CertificateException) {
            throw SavedCertificateException(e, x509Certificates)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> =
            trustManager.acceptedIssuers
}

class SavingExtendedTrustManager(
        private val trustManager: X509ExtendedTrustManager
) : X509ExtendedTrustManager() {

    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String,
                                    socket: Socket) {
        try {
            trustManager.checkClientTrusted(x509Certificates, s, socket)
        } catch (e: CertificateException) {
            throw SavedCertificateException(e, x509Certificates)
        }
    }

    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String,
                                    socket: Socket) {
        try {
            trustManager.checkServerTrusted(x509Certificates, s, socket)
        } catch (e: CertificateException) {
            throw SavedCertificateException(e, x509Certificates)
        }
    }

    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String,
                                    sslEngine: SSLEngine) {
        try {
            trustManager.checkClientTrusted(x509Certificates, s, sslEngine)
        } catch (e: CertificateException) {
            throw SavedCertificateException(e, x509Certificates)
        }
    }

    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String,
                                    sslEngine: SSLEngine) {
        try {
            trustManager.checkServerTrusted(x509Certificates, s, sslEngine)
        } catch (e: CertificateException) {
            throw SavedCertificateException(e, x509Certificates)
        }
    }

    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String) {
        try {
            trustManager.checkClientTrusted(x509Certificates, s)
        } catch (e: CertificateException) {
            throw SavedCertificateException(e, x509Certificates)
        }
    }

    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String) {
        try {
            trustManager.checkServerTrusted(x509Certificates, s)
        } catch (e: CertificateException) {
            throw SavedCertificateException(e, x509Certificates)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> =
            trustManager.acceptedIssuers
}
