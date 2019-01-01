/*
 * Copyright 2012-2019 Tobi29
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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.shouldEqual
import org.tobi29.io.*
import org.tobi29.stdex.atomic.AtomicLong
import java.nio.channels.Pipe
import java.security.KeyStoreException
import javax.net.ssl.KeyManager

object SSLChannelTests : Spek({
    describe("an SSL channel") {
        // No certificate checking with in memory connection
        val sslServer = SSLHandle(getKeyManagers())
        val sslClient = SSLHandle.insecure()
        describe("writing data through a pipe") {
            val success = AtomicLong(0L)

            val address = RemoteAddress("test.dummy", 0)
            val pipe1 = Pipe.open()
            val pipe2 = Pipe.open()
            val sourceLeft = pipe1.source().apply {
                configureBlocking(false)
            }.toChannel()
            val sinkRight = pipe1.sink().apply {
                configureBlocking(false)
            }.toChannel()
            val sourceRight = pipe2.source().apply {
                configureBlocking(false)
            }.toChannel()
            val sinkLeft = pipe2.sink().apply {
                configureBlocking(false)
            }.toChannel()

            val channelLeft = sslClient.newSSLChannel(
                address,
                sslServer.newSSLChannel(
                    address, sourceLeft, sinkLeft,
                    Dispatchers.Default, false
                ), Dispatchers.Default, true
            )
            val channelRight = sslServer.newSSLChannel(
                address,
                sslClient.newSSLChannel(
                    address, sourceRight, sinkRight,
                    Dispatchers.Default, true
                ), Dispatchers.Default, false
            )

            runBlocking {
                val job1 = launch(Dispatchers.Default) {
                    val bufferSend =
                        ByteBuffer(1024).apply { fill { 42 }._flip() }
                    val bufferReceive = ByteBuffer(1024)
                    while (bufferSend.hasRemaining()) {
                        channelLeft.write(bufferSend)
                        yield()
                    }
                    while (bufferReceive.hasRemaining()) {
                        channelLeft.read(bufferReceive)
                        yield()
                    }
                    bufferReceive._flip()
                    while (bufferReceive.hasRemaining()) {
                        if (bufferReceive.get() != 43.toByte()) {
                            throw AssertionError("Invalid received byte")
                        }
                    }
                    bufferSend._clear()._limit(123)
                    bufferSend.fill { 44 }._flip()
                    while (bufferSend.hasRemaining()) {
                        channelLeft.write(bufferSend)
                        yield()
                    }
                    bufferReceive._clear()._limit(123)
                    while (bufferReceive.hasRemaining()) {
                        channelLeft.read(bufferReceive)
                        yield()
                    }
                    bufferReceive._flip()
                    while (bufferReceive.hasRemaining()) {
                        if (bufferReceive.get() != 45.toByte()) {
                            throw AssertionError("Invalid received byte")
                        }
                    }
                    channelLeft.finishAsync()
                    channelLeft.close()

                    success.getAndIncrement()
                }

                val job2 = launch(Dispatchers.Default) {
                    val bufferSend =
                        ByteBuffer(1024).apply { fill { 43 }._flip() }
                    while (bufferSend.hasRemaining()) {
                        channelRight.write(bufferSend)
                        yield()
                    }
                    val bufferReceive = ByteBuffer(1024)
                    while (bufferReceive.hasRemaining()) {
                        channelRight.read(bufferReceive)
                        yield()
                    }
                    bufferReceive._flip()
                    while (bufferReceive.hasRemaining()) {
                        if (bufferReceive.get() != 42.toByte()) {
                            throw AssertionError("Invalid received byte")
                        }
                    }
                    bufferSend._clear()._limit(123)
                    bufferSend.fill { 45 }._flip()
                    while (bufferSend.hasRemaining()) {
                        channelRight.write(bufferSend)
                        yield()
                    }
                    bufferReceive._clear()._limit(123)
                    while (bufferReceive.hasRemaining()) {
                        channelRight.read(bufferReceive)
                        yield()
                    }
                    bufferReceive._flip()
                    while (bufferReceive.hasRemaining()) {
                        if (bufferReceive.get() != 44.toByte()) {
                            throw AssertionError("Invalid received byte")
                        }
                    }
                    channelRight.finishAsync()
                    channelRight.close()

                    success.getAndIncrement()
                }

                job1.join()
                job2.join()
            }

            it("should successfully handle both streams") {
                success.get() shouldEqual 2L
            }
        }
    }
})

private fun getKeyManagers(): Array<KeyManager> {
    return try {
        val keyStore = keyStore(
            "default.p12", "storepass",
            SSLChannelTests::class.java.classLoader
        )
        keyManagers(keyStore, "storepass")
    } catch (e: KeyStoreException) {
        throw IOException(e)
    }
}
