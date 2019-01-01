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

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class CertificateX509TrustManager(
        private val certificates: Array<X509Certificate>) : X509TrustManager {
    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String?) {
        if (x509Certificates.any {
            !certificates.contains(it)
        }) {
            throw CertificateException(
                    "Untrusted certificate")
        }
    }

    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String?) =
            checkClientTrusted(x509Certificates, s)

    override fun getAcceptedIssuers() = certificates
}

object DummyX509TrustManager : X509TrustManager {
    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String?) {
    }

    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>,
                                    s: String?) =
            checkClientTrusted(x509Certificates, s)

    override fun getAcceptedIssuers() = emptyArray<X509Certificate>()
}
