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

package org.tobi29.scapes.engine.server.tests

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.yield
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.server.*
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.AtomicLong
import org.tobi29.scapes.engine.utils.io.ByteBuffer
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.io.fill
import java.nio.channels.Pipe
import java.security.KeyStoreException
import javax.net.ssl.KeyManager

object SSLChannelTests : Spek({
    describe("an SSL channel") {
        // No certificate checking with in memory connection
        val sslServer = SSLHandle(getKeyManagers())
        val sslClient = SSLHandle.insecure()
        on("writing data through a pipe") {
            val taskExecutor = CommonPool

            val success = AtomicLong(0L)

            val address = RemoteAddress("test.dummy", 0)
            val pipe1 = Pipe.open()
            val pipe2 = Pipe.open()
            val sourceLeft = pipe1.source().apply { configureBlocking(false) }
            val sinkRight = pipe1.sink().apply { configureBlocking(false) }
            val sourceRight = pipe2.source().apply { configureBlocking(false) }
            val sinkLeft = pipe2.sink().apply { configureBlocking(false) }

            val channelLeft = sslClient.newSSLChannel(address,
                    sslServer.newSSLChannel(address, sourceLeft, sinkLeft,
                            taskExecutor, false), taskExecutor, true)
            val channelRight = sslServer.newSSLChannel(address,
                    sslClient.newSSLChannel(address, sourceRight, sinkRight,
                            taskExecutor, true), taskExecutor, false)

            launch(taskExecutor) {
                val bufferSend = ByteBuffer(1024).apply { fill { 42 }.flip() }
                val bufferReceive = ByteBuffer(1024)
                while (bufferSend.hasRemaining()) {
                    channelLeft.write(bufferSend)
                    yield()
                }
                while (bufferReceive.hasRemaining()) {
                    channelLeft.read(bufferReceive)
                    yield()
                }
                bufferReceive.flip()
                while (bufferReceive.hasRemaining()) {
                    if (bufferReceive.get() != 43.toByte()) {
                        throw AssertionError("Invalid received byte")
                    }
                }
                bufferSend.clear().limit(123)
                bufferSend.fill { 44 }.flip()
                while (bufferSend.hasRemaining()) {
                    channelLeft.write(bufferSend)
                    yield()
                }
                bufferReceive.clear().limit(123)
                while (bufferReceive.hasRemaining()) {
                    channelLeft.read(bufferReceive)
                    yield()
                }
                bufferReceive.flip()
                while (bufferReceive.hasRemaining()) {
                    if (bufferReceive.get() != 45.toByte()) {
                        throw AssertionError("Invalid received byte")
                    }
                }
                channelLeft.finishAsync()

                success.getAndIncrement()
            }

            launch(taskExecutor) {
                val bufferSend = ByteBuffer(1024).apply { fill { 43 }.flip() }
                while (bufferSend.hasRemaining()) {
                    channelRight.write(bufferSend)
                    yield()
                }
                val bufferReceive = ByteBuffer(1024)
                while (bufferReceive.hasRemaining()) {
                    channelRight.read(bufferReceive)
                    yield()
                }
                bufferReceive.flip()
                while (bufferReceive.hasRemaining()) {
                    if (bufferReceive.get() != 42.toByte()) {
                        throw AssertionError("Invalid received byte")
                    }
                }
                bufferSend.clear().limit(123)
                bufferSend.fill { 45 }.flip()
                while (bufferSend.hasRemaining()) {
                    channelRight.write(bufferSend)
                    yield()
                }
                bufferReceive.clear().limit(123)
                while (bufferReceive.hasRemaining()) {
                    channelRight.read(bufferReceive)
                    yield()
                }
                bufferReceive.flip()
                while (bufferReceive.hasRemaining()) {
                    if (bufferReceive.get() != 44.toByte()) {
                        throw AssertionError("Invalid received byte")
                    }
                }
                channelRight.finishAsync()

                success.getAndIncrement()
            }

            it("should successfully handle both streams") {
                success.get() shouldEqual 2L
            }
        }
    }
})

private fun getKeyManagers(): Array<KeyManager> {
    try {
        val keyStore = keyStore("default.p12", "storepass",
                SSLChannelTests::class.java.classLoader)
        return keyManagers(keyStore, "storepass")
    } catch (e: KeyStoreException) {
        throw IOException(e)
    }
}
