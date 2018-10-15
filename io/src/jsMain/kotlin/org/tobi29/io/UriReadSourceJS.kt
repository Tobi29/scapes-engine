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

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.tobi29.arrays.Bytes
import org.tobi29.arrays.BytesRO
import org.tobi29.stdex.asArray
import org.tobi29.utils.Result
import org.tobi29.utils.ResultError
import org.tobi29.utils.ResultOk
import org.tobi29.utils.unwrap
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.File
import org.w3c.xhr.ARRAYBUFFER
import org.w3c.xhr.ProgressEvent
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn

class UriPath(private val uri: Uri) : Path {
    private var requested = false
    private var content: Result<ByteViewBERO, Exception>? = null
    private var queue: ArrayList<() -> Unit>? = ArrayList()

    override fun toUri(): Uri = uri

    override val parent: Path?
        get() = get("..")

    override fun get(path: String): Path =
        UriPath(uri.appendSeparator().resolve(UriRelativePath(path)))

    override fun channel(): ReadableByteChannel {
        request()
        content?.let {
            return ReadableByteStreamChannel(
                MemoryViewReadableStream(it.unwrap())
            )
        }
        return object : ReadableByteChannel {
            private var stream: MemoryViewReadableStream<*>? = null

            override fun read(buffer: Bytes): Int {
                while (true) {
                    stream?.let { return it.getSome(buffer) }
                    stream = MemoryViewReadableStream(
                        (this@UriPath.content ?: return 0).unwrap()
                    )
                }
            }

            override fun isOpen() = true
            override fun close() {}
        }
    }

    override suspend fun <R> readAsync(reader: suspend (ReadableByteStream) -> R): R {
        request()
        return reader(
            MemoryViewReadableStream(suspendCoroutineOrReturn { cont ->
                content?.let { return@suspendCoroutineOrReturn it.unwrap() }
                queue!!.add {
                    val content = content
                    when (content) {
                        is ResultOk -> cont.resume(content.value)
                        is ResultError -> cont.resumeWithException(
                            content.value
                        )
                    }
                }
                COROUTINE_SUSPENDED
            })
        )
    }

    private fun request() {
        if (requested) return
        requested = true

        val request = XMLHttpRequest()
        request.addEventListener("error", { event ->
            event as ProgressEvent
            resume(ResultError(IOException(request.statusText)))
        }, undefined)
        request.addEventListener("abort", { event ->
            event as ProgressEvent
            resume(ResultError(IOException(request.statusText)))
        }, undefined)
        request.addEventListener("load", { event ->
            event as ProgressEvent
            resume(
                if (request.status == 200.toShort() && request.statusText == "OK") {
                    @Suppress("UnsafeCastFromDynamic")
                    val buffer: ArrayBuffer = request.response.asDynamic()
                    ResultOk(
                        Int8Array(buffer, 0, buffer.byteLength).asArray().viewBE
                    )
                } else ResultError(IOException(request.statusText))
            )
        }, undefined)

        request.open("GET", uri.toString(), true)
        request.responseType = XMLHttpRequestResponseType.ARRAYBUFFER

        request.send(undefined)
    }

    private fun resume(content: Result<ByteViewBERO, Exception>) {
        this.content = content
        queue?.forEach { it() }
        queue = null
    }
}

suspend fun <R> Blob.useUri(block: suspend (Uri) -> R): R {
    val url = URL.createObjectURL(this)
    return try {
        block(Uri(url))
    } finally {
        URL.revokeObjectURL(url)
    }
}

suspend fun <R> BytesRO.useUri(block: suspend (Uri) -> R): R =
    File(arrayOf(readAsInt8Array()), "").useUri(block)

suspend fun <R> ReadSource.useUri(block: suspend (Uri) -> R): R =
    toUri().let { uri ->
        if (uri == null) data().useUri(block)
        else block(uri)
    }
