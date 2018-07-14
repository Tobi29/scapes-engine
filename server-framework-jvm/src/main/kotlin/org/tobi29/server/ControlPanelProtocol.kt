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

import kotlinx.coroutines.experimental.channels.LinkedListChannel
import kotlinx.coroutines.experimental.yield
import org.tobi29.io.IOException
import org.tobi29.io.tag.*
import org.tobi29.io.tag.binary.readBinary
import org.tobi29.io.tag.binary.writeBinary
import org.tobi29.io.view
import org.tobi29.logging.KLogging
import org.tobi29.stdex.ConcurrentHashMap
import org.tobi29.stdex.Throws
import org.tobi29.stdex.computeAbsent
import org.tobi29.utils.EventDispatcher
import org.tobi29.utils.ListenerRegistrar
import org.tobi29.utils.systemClock
import java.security.*
import java.security.spec.InvalidKeySpecException
import javax.crypto.*
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec
import kotlin.collections.set

/**
 * Simple tcp protocol to send [TagMap]s back and forth
 */
open class ControlPanelProtocol(private val worker: ConnectionWorker,
                                private val channel: PacketBundleChannel,
                                listenerParent: EventDispatcher) {
    /**
     * [EventDispatcher] for this object
     */
    val events = EventDispatcher(listenerParent) { listeners() }

    /**
     * Returns the client ID of this connection
     * @throws IllegalStateException when no connection is established yet on server side
     */
    val id: String
        get() {
            return idStr ?: throw IllegalStateException(
                    "Control panel not authenticated")
        }

    private var idStr: String? = null
    private val queue = LinkedListChannel<TagMap>()
    private val openHooks = LinkedListChannel<() -> Unit>()
    private val commands = ConcurrentHashMap<String, Pair<MutableList<(TagMap) -> Unit>, LinkedListChannel<(TagMap) -> Unit>>>()
    private var pingWait = 0L
    var ping = 0L
        private set

    init {
        addCommand("Commands-List") {
            send("Commands-Send", TagMap {
                this["Commands"] = TagList {
                    commands.keys.forEach {
                        add(it.toTag())
                    }
                }
            })
        }
    }

    open fun ListenerRegistrar.listeners() {}

    @Throws(IOException::class)
    suspend fun runClient(connection: Connection,
                          client: String,
                          authentication: (String, Int, ByteArray) -> Cipher) {
        idStr = client
        try {
            if (loginClient(client, authentication)) {
                return
            }
            open(connection)
        } finally {
            close()
        }
    }

    @Throws(IOException::class)
    suspend fun runClientAsym(connection: Connection,
                              client: String,
                              authentication: (String, Int) -> Cipher) {
        idStr = client
        try {
            if (loginClientAsym(client, authentication)) {
                return
            }
            open(connection)
        } finally {
            close()
        }
    }

    @Throws(IOException::class)
    suspend fun runServer(connection: Connection,
                          authentication: (String, Int, ByteArray) -> Cipher?) {
        try {
            if (challengeServer(authentication)) {
                return
            }
            open(connection)
        } finally {
            close()
        }
    }

    @Throws(IOException::class)
    suspend fun runServerAsym(connection: Connection,
                              authentication: (String, Int) -> Cipher?) {
        try {
            if (challengeServerAsym(authentication)) {
                return
            }
            open(connection)
        } finally {
            close()
        }
    }

    /**
     * Sends the given command with the payload
     *
     * **Note:** The command will be sent asynchronously and this method returns
     * immediately
     *
     * **Note:** This does not make a deep copy of [payload], hence it should
     * not be used anymore after calling this
     * @param command Command name
     * @param payload [TagMap] containing data to send with the command
     */
    fun send(command: String,
             payload: TagMap) {
        queue.offer(TagMap {
            this["Command"] = command.toTag()
            this["Payload"] = payload
        })
        worker.wake()
    }

    /**
     * Adds a receiver for the specified command
     *
     * **Note:** Adding multiple receivers for the same command will call all
     * receivers that got ever added
     * **Note:** The payload is not deep-copied between invocations to
     * receivers, so care should be taken when keeping it
     * @param command Command name
     * @param consumer Callback to receive the payload
     */
    fun addCommand(command: String,
                   consumer: (TagMap) -> Unit) {
        val list = commands.computeAbsent(command) {
            Pair(ArrayList(), LinkedListChannel())
        }
        synchronized(list.first) {
            list.first.add(consumer)
        }
    }

    /**
     * Runnable that gets executed once the connection is established and
     * authenticated
     * @param runnable Callback that gets called
     */
    fun openHook(runnable: () -> Unit) {
        openHooks.offer(runnable)
    }

    /**
     * Runnable that gets executed when receiving the specified command for the
     * first time after calling this
     * @param command The name of the command to listen to
     * @param runnable Callback that gets called with the payload
     */
    fun commandHook(command: String,
                    runnable: (TagMap) -> Unit) {
        val list = commands.computeAbsent(command) {
            Pair(ArrayList(), LinkedListChannel())
        }
        list.second.offer(runnable)
    }

    override fun toString(): String {
        return channel.toString()
    }

    private fun processCommand(command: String,
                               payload: TagMap) {
        val consumer = commands[command]
        if (consumer != null) {
            while (true) {
                val onceListener = consumer.second.poll() ?: break
                onceListener(payload)
            }
            synchronized(consumer.first) {
                consumer.first.forEach { it(payload) }
            }
        }
    }

    private suspend fun loginClient(client: String,
                                    authentication: (String, Int, ByteArray) -> Cipher): Boolean {
        channel.outputStream.putString(client)
        channel.queueBundle()
        if (channel.receive()) {
            return true
        }
        val challenge = ByteArray(CHALLENGE_CIPHER_LENGTH)
        val salt = ByteArray(SALT_LENGTH)
        channel.inputStream.get(challenge.view)
        channel.inputStream.get(salt.view)
        try {
            val cipher = authentication(client, Cipher.DECRYPT_MODE, salt)
            channel.outputStream.put(cipher.doFinal(challenge).view)
        } catch (e: IllegalBlockSizeException) {
            throw IOException(e)
        } catch (e: BadPaddingException) {
            throw IOException(e)
        }
        channel.queueBundle()
        while (!openHooks.isEmpty) {
            openHooks.poll()?.invoke()
        }
        return false
    }

    private suspend fun challengeServer(authentication: (String, Int, ByteArray) -> Cipher?): Boolean {
        if (channel.receive()) {
            return true
        }
        val id = channel.inputStream.getString(1024)
        val challenge = ByteArray(CHALLENGE_LENGTH)
        val salt = ByteArray(SALT_LENGTH)
        val random = SecureRandom()
        random.nextBytes(challenge)
        random.nextBytes(salt)
        try {
            val cipher = authentication(id, Cipher.ENCRYPT_MODE,
                    salt) ?: throw IOException("Unknown id")
            channel.outputStream.put(cipher.doFinal(challenge).view)
            channel.outputStream.put(salt.view)
        } catch (e: IllegalBlockSizeException) {
            throw IOException(e)
        } catch (e: BadPaddingException) {
            throw IOException(e)
        }
        channel.queueBundle()
        return loginServer(id, challenge)
    }

    private suspend fun loginClientAsym(client: String,
                                        authentication: (String, Int) -> Cipher): Boolean {
        channel.outputStream.putString(client)
        channel.queueBundle()
        if (channel.receive()) {
            return true
        }
        val challenge = ByteArray(ASYM_CHALLENGE_CIPHER_LENGTH)
        channel.inputStream.get(challenge.view)
        try {
            val cipher = authentication(client, Cipher.DECRYPT_MODE)
            channel.outputStream.put(cipher.doFinal(challenge).view)
        } catch (e: IllegalBlockSizeException) {
            throw IOException(e)
        } catch (e: BadPaddingException) {
            throw IOException(e)
        }
        channel.queueBundle()
        while (!openHooks.isEmpty) {
            openHooks.poll()?.invoke()
        }
        return false
    }

    private suspend fun challengeServerAsym(authentication: (String, Int) -> Cipher?): Boolean {
        if (channel.receive()) {
            return true
        }
        val id = channel.inputStream.getString(1024)
        val challenge = ByteArray(ASYM_CHALLENGE_LENGTH)
        val random = SecureRandom()
        random.nextBytes(challenge)
        try {
            val cipher = authentication(id,
                    Cipher.ENCRYPT_MODE) ?: throw IOException("Unknown id")
            channel.outputStream.put(cipher.doFinal(challenge).view)
        } catch (e: IllegalBlockSizeException) {
            throw IOException(e)
        } catch (e: BadPaddingException) {
            throw IOException(e)
        }
        channel.queueBundle()
        return loginServer(id, challenge)
    }

    private suspend fun loginServer(id: String,
                                    challenge: ByteArray): Boolean {
        if (channel.receive()) {
            return true
        }
        val check = ByteArray(challenge.size)
        channel.inputStream.get(check.view)
        if (!(check contentEquals challenge)) {
            throw ConnectionCloseException("Failed password authentication")
        }
        idStr = id
        while (!openHooks.isEmpty) {
            openHooks.poll()?.invoke()
        }
        return false
    }

    private suspend fun open(connection: Connection) {
        try {
            pingWait = systemClock.timeMillis() + 1000
            while (!connection.shouldClose) {
                val currentTime = systemClock.timeMillis()
                if (pingWait < currentTime) {
                    pingWait = currentTime + 1000
                    TagMap { this["Ping"] = currentTime.toTag() }.writeBinary(
                            channel.outputStream)
                    channel.queueBundle()
                }
                openSend()
                if (openReceive(connection)) {
                    break
                }
                yield()
            }
        } catch (e: ConnectionCloseException) {
            logger.info { "Disconnecting control panel: ${e.message}" }
        }
    }

    private suspend fun openReceive(connection: Connection): Boolean {
        loop@ while (true) {
            when (channel.process()) {
                PacketBundleChannel.FetchResult.CLOSED -> return true
                PacketBundleChannel.FetchResult.YIELD -> break@loop
                PacketBundleChannel.FetchResult.BUNDLE -> {
                    val tagStructure = readBinary(channel.inputStream)
                    tagStructure["Ping"]?.toLong()?.let {
                        TagMap { this["Pong"] = it.toTag() }.writeBinary(
                                channel.outputStream)
                        channel.queueBundle()
                    }
                    tagStructure["Pong"]?.toLong()?.let {
                        val ping = systemClock.timeMillis() - it
                        this.ping = ping
                        connection.increaseTimeout(10000 - ping)
                    }
                    tagStructure["Commands"]?.toList()?.asSequence()
                            ?.mapNotNull(
                                    Tag::toMap)?.forEach { commandStructure ->
                        val command = commandStructure["Command"]?.toString() ?: throw IOException(
                                "Command without id")
                        val payload = commandStructure["Payload"]?.toMap() ?: throw IOException(
                                "Command without payload")
                        processCommand(command, payload)
                    }
                }
            }
        }
        return false
    }

    private suspend fun openSend() {
        val list = ArrayList<TagMap>(0)
        while (!queue.isEmpty) {
            queue.poll()?.let { list.add(it) }
        }
        if (!list.isEmpty()) {
            TagMap { this["Commands"] = list.toTag() }.writeBinary(
                    channel.outputStream)
            channel.queueBundle()
        }
    }

    private fun close() {
        channel.close()
        events.disable()
    }

    companion object : KLogging() {
        private val CHALLENGE_LENGTH = 1 shl 10 shl 2
        private val SALT_LENGTH = 8
        private val CHALLENGE_CIPHER_LENGTH = CHALLENGE_LENGTH + SALT_LENGTH
        private val ASYM_CHALLENGE_LENGTH = 501
        private val ASYM_CHALLENGE_CIPHER_LENGTH = 512

        /**
         * Password and salt based authentication
         * @param mode Mode as passed by the [ControlPanelProtocol] object
         * @param salt Salt as passed by the [ControlPanelProtocol] object
         * @param password The password to use for authentication
         * @return A [Cipher] for the [ControlPanelProtocol]
         */
        fun passwordAuthentication(mode: Int,
                                   salt: ByteArray,
                                   password: String): Cipher {
            try {
                val keySpec = PBEKeySpec(password.toCharArray())
                val keyFactory = SecretKeyFactory.getInstance(
                        "PBEWithMD5AndDES")
                val key = keyFactory.generateSecret(keySpec)
                val pbeParamSpec = PBEParameterSpec(salt, 31)
                val cipher = Cipher.getInstance("PBEWithMD5AndDES")
                cipher.init(mode, key, pbeParamSpec)
                return cipher
            } catch (e: NoSuchAlgorithmException) {
                throw IOException(e)
            } catch (e: InvalidKeySpecException) {
                throw IOException(e)
            } catch (e: NoSuchPaddingException) {
                throw IOException(e)
            } catch (e: InvalidKeyException) {
                throw IOException(e)
            } catch (e: InvalidAlgorithmParameterException) {
                throw IOException(e)
            }
        }

        /**
         * Password and salt based authentication
         * @param password The password to use for authentication
         * @return A function that implements the authentication parameter
         */
        fun passwordAuthentication(password: String): (String, Int, ByteArray) -> Cipher = { id, mode, salt ->
            passwordAuthentication(mode, salt, password)
        }

        /**
         * Password and salt based authentication
         * @param mode Mode as passed by the [ControlPanelProtocol] object
         * @param publicKey The [PublicKey] to use for authentication
         * @return A [Cipher] for the [ControlPanelProtocol]
         */
        fun keyPairAuthentication(mode: Int,
                                  publicKey: PublicKey): Cipher {
            return try {
                val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                cipher.init(mode, publicKey)
                cipher
            } catch (e: NoSuchAlgorithmException) {
                throw IOException(e)
            } catch (e: InvalidKeySpecException) {
                throw IOException(e)
            } catch (e: NoSuchPaddingException) {
                throw IOException(e)
            } catch (e: InvalidKeyException) {
                throw IOException(e)
            } catch (e: InvalidAlgorithmParameterException) {
                throw IOException(e)
            }
        }

        /**
         * Password and salt based authentication
         * @param publicKey The [PublicKey] to use for authentication
         * @return A function that implements the authentication parameter
         */
        fun keyPairAuthentication(publicKey: PublicKey): (String, Int) -> Cipher = { id, mode ->
            keyPairAuthentication(mode, publicKey)
        }

        /**
         * Password and salt based authentication
         * @param mode Mode as passed by the [ControlPanelProtocol] object
         * @param privateKey The [PrivateKey] to use for authentication
         * @return A [Cipher] for the [ControlPanelProtocol]
         */
        fun keyPairAuthentication(mode: Int,
                                  privateKey: PrivateKey): Cipher {
            return try {
                val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                cipher.init(mode, privateKey)
                cipher
            } catch (e: NoSuchAlgorithmException) {
                throw IOException(e)
            } catch (e: InvalidKeySpecException) {
                throw IOException(e)
            } catch (e: NoSuchPaddingException) {
                throw IOException(e)
            } catch (e: InvalidKeyException) {
                throw IOException(e)
            } catch (e: InvalidAlgorithmParameterException) {
                throw IOException(e)
            }
        }

        /**
         * Password and salt based authentication
         * @param privateKey The [PrivateKey] to use for authentication
         * @return A function that implements the authentication parameter
         */
        fun keyPairAuthentication(privateKey: PrivateKey): (String, Int) -> Cipher = { id, mode ->
            keyPairAuthentication(mode, privateKey)
        }
    }
}
