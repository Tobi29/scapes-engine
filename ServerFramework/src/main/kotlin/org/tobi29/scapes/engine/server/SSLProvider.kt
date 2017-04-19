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

import org.tobi29.scapes.engine.server.spi.SSLProviderProvider
import org.tobi29.scapes.engine.utils.UnsupportedJVMException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.KeyManager

object SSLProvider {
    private val IMPL = loadService()

    private fun loadService(): SSLProviderImpl {
        for (trustManager in ServiceLoader.load(
                SSLProviderProvider::class.java)) {
            try {
                if (trustManager.available()) {
                    return trustManager.implementation()
                }
            } catch (e: ServiceConfigurationError) {
            }

        }
        throw UnsupportedJVMException(
                "No trust manager implementation available")
    }

    // TODO: @Throws(IOException::class)
    fun sslHandle(feedback: ((Array<X509Certificate>) -> Boolean)?): SSLHandle {
        return sslHandle(null, feedback)
    }

    // TODO: @Throws(IOException::class)
    fun sslHandle(keyManagers: Array<KeyManager>? = null,
                  feedback: ((Array<X509Certificate>) -> Boolean)? = null): SSLHandle {
        return IMPL.sslHandle(keyManagers, feedback)
    }
}
