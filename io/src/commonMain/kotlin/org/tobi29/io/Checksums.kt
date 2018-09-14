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

package org.tobi29.io

import org.tobi29.checksums.Checksum
import org.tobi29.checksums.ChecksumAlgorithm

// TODO: Remove after 0.0.14

/**
 * Creates a checksum from the given [ReadableByteStream]
 * @param input     [ReadableByteStream] that will be used to create the checksum
 * @param algorithm The algorithm that will be used to create the checksum
 * @return A [Checksum] containing the checksum
 * @throws IOException When an IO error occurs
 */
@Deprecated("Will be dropped without direct replacement")
fun checksum(
    input: ReadableByteStream,
    algorithm: ChecksumAlgorithm = ChecksumAlgorithm.Sha256
): Checksum {
    val ctx = algorithm.createContext()
    input.process { ctx.update(it) }
    return Checksum(algorithm, ctx.finish())
}
