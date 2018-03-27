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

package org.tobi29.checksums

import org.tobi29.arrays.BytesRO
import org.tobi29.arrays.HeapBytes
import org.tobi29.stdex.UnsupportedJVMException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

// TODO: Make createContext not abstract
actual sealed class ChecksumAlgorithm(
    private val digestName: String,
    actual val bytes: Int,
    actual val name: String
) {
    actual abstract fun createContext(): ChecksumContext

    object Sha512 : ChecksumAlgorithm("SHA-512", 64, "SHA512") {
        override fun createContext(): ChecksumContext =
            MessageDigestChecksumContext(digest())
    }

    actual object Sha256 : ChecksumAlgorithm("SHA-256", 32, "SHA256") {
        actual override fun createContext(): ChecksumContext =
            MessageDigestChecksumContext(digest())
    }

    object Sha1 : ChecksumAlgorithm("SHA1", 20, "SHA1") {
        override fun createContext(): ChecksumContext =
            MessageDigestChecksumContext(digest())
    }

    object Md5 : ChecksumAlgorithm("MD5", 16, "MD5") {
        override fun createContext(): ChecksumContext =
            MessageDigestChecksumContext(digest())
    }

    /**
     * Creates a new [MessageDigest]
     * @return [MessageDigest] using the specified algorithm
     */
    fun digest(): MessageDigest {
        return try {
            MessageDigest.getInstance(digestName)
        } catch (e: NoSuchAlgorithmException) {
            throw UnsupportedJVMException(e)
        }
    }
}

private class MessageDigestChecksumContext(
    private val digest: MessageDigest
) : ChecksumContext {
    override fun update(data: ByteArray) {
        digest.update(data)
    }

    override fun update(data: BytesRO) {
        if (data is HeapBytes) {
            digest.update(data.array, data.offset, data.size)
        } else {
            // TODO: Can we optimize this?
            // TODO: Cannot access ByteBuffer here, fix?
            for (i in 0 until data.size) {
                digest.update(data[i])
            }
        }
    }

    override fun finish(): ByteArray = digest.digest()
}
