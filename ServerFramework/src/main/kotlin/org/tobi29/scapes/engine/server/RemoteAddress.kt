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

import org.tobi29.scapes.engine.utils.io.tag.*

import java.net.InetSocketAddress

class RemoteAddress : TagMapWrite {
    val address: String
    val port: Int

    constructor(address: String,
                port: Int) {
        this.address = address
        this.port = port
    }

    constructor(address: InetSocketAddress) {
        this.address = address.hostString
        port = address.port
    }

    override fun write(map: ReadWriteTagMap) {
        map["Address"] = address
        map["Port"] = port
    }
}

fun RemoteAddress(map: ReadTagMutableMap): RemoteAddress {
    val address = map["Address"].toString()
    val port = map["Port"]?.toInt() ?: -1
    return RemoteAddress(address, port)
}
