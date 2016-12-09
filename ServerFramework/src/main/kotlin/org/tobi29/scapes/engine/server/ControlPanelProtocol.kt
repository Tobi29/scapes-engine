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
import mu.KLogging
import org.tobi29.scapes.engine.utils.EventDispatcher
import org.tobi29.scapes.engine.utils.ListenerOwner
import org.tobi29.scapes.engine.utils.ListenerOwnerHandle
import org.tobi29.scapes.engine.utils.io.RandomReadableByteStream
import org.tobi29.scapes.engine.utils.io.RandomWritableByteStream
import org.tobi29.scapes.engine.utils.io.tag.TagStructure
import org.tobi29.scapes.engine.utils.io.tag.binary.TagStructureBinary
import org.tobi29.scapes.engine.utils.io.tag.getListStructure
import org.tobi29.scapes.engine.utils.io.tag.structure
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
open class ControlPanelProtocol private constructor(
        private val worker: ConnectionWorker,
        private val channel: PacketBundleChannel,
        events: EventDispatcher?) : Connection, ListenerOwner {
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
    private val closeHooks = ConcurrentLinkedQueue<() -> Unit>()
    private val disconnectHooks = ConcurrentLinkedQueue<(Exception) -> Unit>()
    private val commands = ConcurrentHashMap<String, Pair<MutableList<(TagStructure) -> Unit>, Queue<(TagStructure) -> Unit>>>()
    private var state: ChannelState? = null
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

    /**
     * Construct a client-side instance for authentication with a salt
     * @param worker The [ConnectionWorker] this will run in
     * @param channel The [PacketBundleChannel] to communicate with
     * @param events Optional parent [EventDispatcher]
     * @param client Client ID
     * @param authentication Authentication mechanism
     * @throws IOException When an I/O error occured whilst registering to the worker's selector
     */
    @Throws(IOException::class)
    constructor(worker: ConnectionWorker,
                channel: PacketBundleChannel,
                events: EventDispatcher?,
                client: String,
                authentication: (String, Int, ByteArray) -> Cipher) : this(
            worker, channel, events) {
        idStr = client
        val output = channel.outputStream
        output.putString(client)
        channel.queueBundle()
        state = ChannelState(
                { i, o -> loginClient(i, o, client, authentication) })
    }

    /**
     * Construct a client-side instance for authentication without a salt
     * @param worker The [ConnectionWorker] this will run in
     * @param channel The [PacketBundleChannel] to communicate with
     * @param events Optional parent [EventDispatcher]
     * @param client Client ID
     * @param authentication Authentication mechanism
     */
    @Throws(IOException::class)
    constructor(worker: ConnectionWorker,
                channel: PacketBundleChannel,
                events: EventDispatcher?,
                client: String,
                authentication: (String, Int) -> Cipher) : this(worker, channel,
            events) {
        idStr = client
        val output = channel.outputStream
        output.putString(client)
        channel.queueBundle()
        state = ChannelState(
                { i, o -> loginClientAsym(i, o, client, authentication) })
    }

    /**
     * Construct a server-side instance for authentication with a salt
     * @param worker The [ConnectionWorker] this will run in
     * @param channel The [PacketBundleChannel] to communicate with
     * @param events Optional parent [EventDispatcher]
     * @param authentication Authentication mechanism
     */
    @Throws(IOException::class)
    constructor(worker: ConnectionWorker,
                channel: PacketBundleChannel,
                events: EventDispatcher?,
                authentication: (String, Int, ByteArray) -> Cipher?) : this(
            worker, channel, events) {
        state = ChannelState(
                { i, o -> challengeServer(i, o, authentication) })
    }

    /**
     * Construct a server-side instance for authentication without a salt
     * @param worker The [ConnectionWorker] this will run in
     * @param channel The [PacketBundleChannel] to communicate with
     * @param events Optional parent [EventDispatcher]
     * @param authentication Authentication mechanism
     */
    @Throws(IOException::class)
    constructor(worker: ConnectionWorker,
                channel: PacketBundleChannel,
                events: EventDispatcher?,
                authentication: (String, Int) -> Cipher?) : this(worker,
            channel, events) {
        state = ChannelState(
                { i, o -> challengeServerAsym(i, o, authentication) })
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
     * Runnable that gets executed once the connection is closed
     *
     * **Note:** This is called even when the connection breaks
     * @param runnable Callback that gets called
     */
    fun closeHook(runnable: () -> Unit) {
        closeHooks.add(runnable)
    }

    /**
     * Runnable that gets executed in case the connection breaks
     * @param runnable Callback that gets called with the exception that occured
     */
    fun disconnectHook(runnable: (Exception) -> Unit) {
        disconnectHooks.add(runnable)
    }

    override fun tick(worker: ConnectionWorker) {
        try {
            if (channel.process2({ state })) {
                state = null
            }
        } catch (e: ConnectionCloseException) {
            logger.info { "Disconnecting control panel: $e" }
            state = ChannelState()
        } catch (e: InvalidPacketDataException) {
            logger.info { "Disconnecting control panel: $e" }
            state = ChannelState()
        } catch (e: IOException) {
            logger.info { "Control panel disconnected: $e" }
            while (!disconnectHooks.isEmpty()) {
                disconnectHooks.poll()(e)
            }
            state = null
        }
    }

    override val isClosed: Boolean
        get() = state == null

    override fun requestClose() {
        channel.requestClose()
    }

    override fun close() {
        while (!closeHooks.isEmpty()) {
            closeHooks.poll()()
        }
        channel.close()
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

    private fun loginClient(input: RandomReadableByteStream,
                            output: RandomWritableByteStream,
                            client: String,
                            authentication: (String, Int, ByteArray) -> Cipher): Boolean {
        val challenge = ByteArray(CHALLENGE_CIPHER_LENGTH)
        val salt = ByteArray(SALT_LENGTH)
        input[challenge]
        input[salt]
        try {
            val cipher = authentication(client, Cipher.DECRYPT_MODE, salt)
            output.put(cipher.doFinal(challenge))
        } catch (e: IllegalBlockSizeException) {
            throw IOException(e)
        } catch (e: BadPaddingException) {
            throw IOException(e)
        }
        while (!openHooks.isEmpty()) {
            openHooks.poll()()
        }
        state = ChannelState({ i, o -> open(i) }, { openSend(it) })
        return true
    }

    private fun challengeServer(input: RandomReadableByteStream,
                                output: RandomWritableByteStream,
                                authentication: (String, Int, ByteArray) -> Cipher?): Boolean {
        val id = input.getString(1024)
        val challenge = ByteArray(CHALLENGE_LENGTH)
        val salt = ByteArray(SALT_LENGTH)
        val random = SecureRandom()
        random.nextBytes(challenge)
        random.nextBytes(salt)
        try {
            val cipher = authentication(id, Cipher.ENCRYPT_MODE,
                    salt) ?: throw IOException("Unknown id")
            output.put(cipher.doFinal(challenge))
            output.put(salt)
        } catch (e: IllegalBlockSizeException) {
            throw IOException(e)
        } catch (e: BadPaddingException) {
            throw IOException(e)
        }
        state = ChannelState({ i, o -> loginServer(i, id, challenge) })
        return true
    }

    private fun loginClientAsym(input: RandomReadableByteStream,
                                output: RandomWritableByteStream,
                                client: String,
                                authentication: (String, Int) -> Cipher): Boolean {
        val challenge = ByteArray(ASYM_CHALLENGE_CIPHER_LENGTH)
        input[challenge]
        try {
            val cipher = authentication(client, Cipher.DECRYPT_MODE)
            output.put(cipher.doFinal(challenge))
        } catch (e: IllegalBlockSizeException) {
            throw IOException(e)
        } catch (e: BadPaddingException) {
            throw IOException(e)
        }
        while (!openHooks.isEmpty()) {
            openHooks.poll()()
        }
        state = ChannelState({ i, o -> open(i) }, { openSend(it) })
        return true
    }

    private fun challengeServerAsym(input: RandomReadableByteStream,
                                    output: RandomWritableByteStream,
                                    authentication: (String, Int) -> Cipher?): Boolean {
        val id = input.getString(1024)
        val challenge = ByteArray(ASYM_CHALLENGE_LENGTH)
        val random = SecureRandom()
        random.nextBytes(challenge)
        try {
            val cipher = authentication(id,
                    Cipher.ENCRYPT_MODE) ?: throw IOException("Unknown id")
            output.put(cipher.doFinal(challenge))
        } catch (e: IllegalBlockSizeException) {
            throw IOException(e)
        } catch (e: BadPaddingException) {
            throw IOException(e)
        }
        state = ChannelState({ i, o -> loginServer(i, id, challenge) })
        return true
    }

    private fun loginServer(input: RandomReadableByteStream,
                            id: String,
                            challenge: ByteArray): Boolean {
        val check = ByteArray(challenge.size)
        input[check]
        if (!Arrays.equals(check, challenge)) {
            throw ConnectionCloseException(
                    "Failed password authentication")
        }
        idStr = id
        while (!openHooks.isEmpty()) {
            openHooks.poll()()
        }
        state = ChannelState({ i, o -> open(i) }, { openSend(it) })
        return false
    }

    private fun open(input: RandomReadableByteStream): Boolean {
        val tagStructure = TagStructureBinary.read(input)
        tagStructure.getListStructure("Commands") { commandStructure ->
            val command = commandStructure.getString(
                    "Command") ?: throw IOException("Command without id")
            val payload = commandStructure.getStructure(
                    "Payload") ?: throw IOException("Command without payload")
            processCommand(command, payload)
        }
        return false
    }

    private fun openSend(output: RandomWritableByteStream): Boolean {
        val list = ArrayList<TagStructure>(0)
        while (!queue.isEmpty()) {
            list.add(queue.poll())
        }
        if (!list.isEmpty()) {
            val tagStructure = TagStructure()
            tagStructure.setList("Commands", list)
            TagStructureBinary.write(output, tagStructure)
            return true
        }
        return false
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
