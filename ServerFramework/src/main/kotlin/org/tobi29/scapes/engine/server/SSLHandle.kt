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

package org.tobi29.scapes.engine.server

import javax.net.ssl.SSLEngine
import java.io.IOException
import java.security.cert.X509Certificate

interface SSLHandle {
    @Throws(IOException::class)
    fun newEngine(address: RemoteAddress): SSLEngine

    fun certificateFeedback(certificates: Array<X509Certificate>): Boolean

    fun requiresVerification(): Boolean

    @Throws(IOException::class)
    fun verifySession(address: RemoteAddress,
                      engine: SSLEngine,
                      certificates: Array<X509Certificate>)
}
