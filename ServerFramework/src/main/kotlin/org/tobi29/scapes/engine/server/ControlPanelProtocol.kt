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

import java8.util.concurrent.ConcurrentMaps
import kotlinx.coroutines.experimental.yield
import mu.KLogging
import org.tobi29.scapes.engine.utils.EventDispatcher
import org.tobi29.scapes.engine.utils.ListenerOwner
import org.tobi29.scapes.engine.utils.ListenerOwnerHandle
import org.tobi29.scapes.engine.utils.io.tag.*
import org.tobi29.scapes.engine.utils.io.tag.binary.TagStructureBinary
import java.io.IOException
import java.nio.channels.SelectionKey
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.*
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec

/**
 * Simple tcp protocol to send [TagStructure]s back and forth
 */
open class ControlPanelProtocol(private val worker: ConnectionWorker,
                                private val channel: PacketBundleChannel,
                                events: EventDispatcher?) : ListenerOwner {
    /**
     * [EventDispatcher] for this object
     */
    val events = EventDispatcher(events)

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
    private val queue = ConcurrentLinkedQueue<TagStructure>()
    private val openHooks = ConcurrentLinkedQueue<() -> Unit>()
    private val disconnectHooks = ConcurrentLinkedQueue<(Exception) -> Unit>()
    private val commands = ConcurrentHashMap<String, Pair<MutableList<(TagStructure) -> Unit>, Queue<(TagStructure) -> Unit>>>()
    private var isClosed = false
    private var pingWait = 0L
    var ping = 0L
        private set
    override val listenerOwner = ListenerOwnerHandle { !isClosed }

    init {
        channel.register(worker.joiner, SelectionKey.OP_READ)
        addCommand("Commands-List") { payload ->
            val set = commands.keys
            send("Commands-Send", structure {
                setList("Commands", set.toList())
            })
        }
    }

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
        } catch (e: ConnectionCloseException) {
            logger.info { "Disconnecting control panel: $e" }
        } catch (e: InvalidPacketDataException) {
            logger.info { "Disconnecting control panel: $e" }
        } catch (e: IOException) {
            logger.info { "Control panel disconnected: $e" }
            while (!disconnectHooks.isEmpty()) {
                disconnectHooks.poll()(e)
            }
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
        } catch (e: ConnectionCloseException) {
            logger.info { "Disconnecting control panel: $e" }
        } catch (e: InvalidPacketDataException) {
            logger.info { "Disconnecting control panel: $e" }
        } catch (e: IOException) {
            logger.info { "Control panel disconnected: $e" }
            while (!disconnectHooks.isEmpty()) {
                disconnectHooks.poll()(e)
            }
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
        } catch (e: ConnectionCloseException) {
            logger.info { "Disconnecting control panel: $e" }
        } catch (e: InvalidPacketDataException) {
            logger.info { "Disconnecting control panel: $e" }
        } catch (e: IOException) {
            logger.info { "Control panel disconnected: $e" }
            while (!disconnectHooks.isEmpty()) {
                disconnectHooks.poll()(e)
            }
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
        } catch (e: ConnectionCloseException) {
            logger.info { "Disconnecting control panel: $e" }
        } catch (e: InvalidPacketDataException) {
            logger.info { "Disconnecting control panel: $e" }
        } catch (e: IOException) {
            logger.info { "Control panel disconnected: $e" }
            while (!disconnectHooks.isEmpty()) {
                disconnectHooks.poll()(e)
            }
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
     * @param payload [TagStructure] containing data to send with the command
     */
    fun send(command: String,
             payload: TagStructure) {
        queue.add(structure {
            setString("Command", command)
            setStructure("Payload", payload)
        })
        worker.joiner.wake()
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
                   consumer: (TagStructure) -> Unit) {
        val list = ConcurrentMaps.computeIfAbsent(commands,
                command) { Pair(ArrayList(), ConcurrentLinkedQueue()) }
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
        openHooks.add(runnable)
    }

    /**
     * Runnable that gets executed in case the connection breaks
     * @param runnable Callback that gets called with the exception that occurred
     */
    fun disconnectHook(runnable: (Exception) -> Unit) {
        disconnectHooks.add(runnable)
    }

    /**
     * Runnable that gets executed when receiving the specified command for the
     * first time after calling this
     * @param command The name of the command to listen to
     * @param runnable Callback that gets called with the payload
     */
    fun commandHook(command: String,
                    runnable: (TagStructure) -> Unit) {
        val list = ConcurrentMaps.computeIfAbsent(commands,
                command) { Pair(ArrayList(), ConcurrentLinkedQueue()) }
        list.second.add(runnable)
    }

    override fun toString(): String {
        return channel.toString()
    }

    private fun processCommand(command: String,
                               payload: TagStructure) {
        val consumer = commands[command]
        if (consumer != null) {
            while (!consumer.second.isEmpty()) {
                consumer.second.poll()(payload)
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
        channel.inputStream[challenge]
        channel.inputStream[salt]
        try {
            val cipher = authentication(client, Cipher.DECRYPT_MODE, salt)
            channel.outputStream.put(cipher.doFinal(challenge))
        } catch (e: IllegalBlockSizeException) {
            throw IOException(e)
        } catch (e: BadPaddingException) {
            throw IOException(e)
        }
        channel.queueBundle()
        while (!openHooks.isEmpty()) {
            openHooks.poll()()
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
            channel.outputStream.put(cipher.doFinal(challenge))
            channel.outputStream.put(salt)
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
        channel.inputStream[challenge]
        try {
            val cipher = authentication(client, Cipher.DECRYPT_MODE)
            channel.outputStream.put(cipher.doFinal(challenge))
        } catch (e: IllegalBlockSizeException) {
            throw IOException(e)
        } catch (e: BadPaddingException) {
            throw IOException(e)
        }
        channel.queueBundle()
        while (!openHooks.isEmpty()) {
            openHooks.poll()()
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
            channel.outputStream.put(cipher.doFinal(challenge))
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
        channel.inputStream[check]
        if (!Arrays.equals(check, challenge)) {
            throw ConnectionCloseException("Failed password authentication")
        }
        idStr = id
        while (!openHooks.isEmpty()) {
            openHooks.poll()()
        }
        return false
    }

    private suspend fun open(connection: Connection) {
        pingWait = System.currentTimeMillis() + 1000
        while (!connection.shouldClose) {
            val currentTime = System.currentTimeMillis()
            if (pingWait < currentTime) {
                pingWait = currentTime + 1000
                TagStructureBinary.write(channel.outputStream,
                        structure { setLong("Ping", currentTime) })
                channel.queueBundle()
            }
            openSend()
            if (openReceive(connection)) {
                break
            }
            yield()
        }
    }

    private suspend fun openReceive(connection: Connection): Boolean {
        loop@ while (true) {
            when (channel.process()) {
                PacketBundleChannel.FetchResult.CLOSED -> return true
                PacketBundleChannel.FetchResult.YIELD -> break@loop
                PacketBundleChannel.FetchResult.BUNDLE -> {
                    val tagStructure = TagStructureBinary.read(
                            channel.inputStream)
                    tagStructure.getLong("Ping")?.let {
                        TagStructureBinary.write(channel.outputStream,
                                structure { setLong("Pong", it) })
                        channel.queueBundle()
                    }
                    tagStructure.getLong("Pong")?.let {
                        val ping = System.currentTimeMillis() - it
                        this.ping = ping
                        connection.increaseTimeout(10000 - ping)
                    }
                    tagStructure.getListStructure(
                            "Commands") { commandStructure ->
                        val command = commandStructure.getString(
                                "Command") ?: throw IOException(
                                "Command without id")
                        val payload = commandStructure.getStructure(
                                "Payload") ?: throw IOException(
                                "Command without payload")
                        processCommand(command, payload)
                    }
                }
            }
        }
        return false
    }

    private suspend fun openSend() {
        val list = ArrayList<TagStructure>(0)
        while (!queue.isEmpty()) {
            list.add(queue.poll())
        }
        if (!list.isEmpty()) {
            val tagStructure = TagStructure()
            tagStructure.setList("Commands", list)
            TagStructureBinary.write(channel.outputStream, tagStructure)
            channel.queueBundle()
        }
    }

    private fun close() {
        channel.close()
        isClosed = true
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
            try {
                val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                cipher.init(mode, publicKey)
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
            try {
                val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                cipher.init(mode, privateKey)
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
         * @param privateKey The [PrivateKey] to use for authentication
         * @return A function that implements the authentication parameter
         */
        fun keyPairAuthentication(privateKey: PrivateKey): (String, Int) -> Cipher = { id, mode ->
            keyPairAuthentication(mode, privateKey)
        }
    }
}
