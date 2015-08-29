/*
 * Copyright 2012-2015 Tobi29
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
package org.tobi29.scapes.engine.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.utils.io.IOConsumer;
import org.tobi29.scapes.engine.utils.io.PacketBundleChannel;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;
import org.tobi29.scapes.engine.utils.math.FastMath;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ControlPanelProtocol implements Connection {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ControlPanelProtocol.class);
    private static final int SALT_LENGTH = 8, AES_MIN_KEY_LENGTH,
            AES_MAX_KEY_LENGTH;

    static {
        int length = 16;
        try {
            length = Cipher.getMaxAllowedKeyLength("AES") >> 3;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("Failed to detect maximum key length", e);
        }
        length = FastMath.min(length, 32);
        AES_MAX_KEY_LENGTH = length;
        AES_MIN_KEY_LENGTH = FastMath.min(16, length);
    }

    private final PacketBundleChannel channel;
    private final Queue<String[]> queue = new ConcurrentLinkedQueue<>();
    private final Map<String, IOConsumer<String[]>> commands =
            new ConcurrentHashMap<>();
    private final String password;
    private final KeyPair keyPair;
    private State state;
    private byte[] challenge, salt = new byte[SALT_LENGTH];

    private ControlPanelProtocol(String password, SocketChannel channel,
            KeyPair keyPair) {
        this.channel = new PacketBundleChannel(channel, null, null);
        this.password = password;
        this.keyPair = keyPair;
        addCommand("Core:End", command -> {
            throw new ConnectionCloseException("Remote connection end");
        });
        addCommand("Core:CommandsList", command -> send("Core:CommandsSend",
                commands.keySet().stream().toArray(String[]::new)));
    }

    public ControlPanelProtocol(SocketChannel channel, String password) {
        this(password, channel, null);
        state = State.CLIENT_LOGIN_STEP_1;
    }

    public ControlPanelProtocol(SocketChannel channel, String password,
            KeyPair keyPair) throws IOException {
        this(password, channel, keyPair);
        state = State.SERVER_LOGIN_STEP_1;
        WritableByteStream output = this.channel.getOutputStream();
        byte[] array = keyPair.getPublic().getEncoded();
        output.putInt(array.length);
        output.put(array);
        output.putInt(AES_MAX_KEY_LENGTH);
        this.channel.queueBundle();
    }

    public void send(String command, String... arguments) {
        String[] array = new String[arguments.length + 1];
        array[0] = command;
        System.arraycopy(arguments, 0, array, 1, arguments.length);
        queue.add(array);
    }

    private Cipher cipher(int mode, byte[] salt) throws IOException {
        try {
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory keyFactory =
                    SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 31);
            Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
            cipher.init(mode, key, pbeParamSpec);
            return cipher;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new IOException(e);
        }
    }

    private void processCommand(String[] command) throws IOException {
        IOConsumer<String[]> consumer = commands.get(command[0]);
        if (consumer == null) {
            LOGGER.warn("Unknown command: {}", command[0]);
        } else {
            String[] args = new String[command.length - 1];
            System.arraycopy(command, 1, args, 0, args.length);
            consumer.accept(args);
        }
    }

    public void addCommand(String command, IOConsumer<String[]> consumer) {
        commands.put(command, consumer);
    }

    @Override
    public void register(Selector selector, int opt) throws IOException {
        channel.register(selector, opt);
    }

    @Override
    public boolean tick(AbstractServerConnection.NetWorkerThread worker) {
        try {
            return tick();
        } catch (ConnectionCloseException | InvalidPacketDataException e) {
            LOGGER.info("Disconnecting control panel: {}", e.toString());
            state = State.CLOSING;
        } catch (IOException e) {
            LOGGER.info("Control panel disconnected: {}", e.toString());
            state = State.CLOSED;
        }
        return true;
    }

    @Override
    public boolean isClosed() {
        return state == State.CLOSED;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    public boolean tick() throws IOException {
        boolean processing = false;
        Optional<ReadableByteStream> bundle = channel.fetch();
        if (bundle.isPresent()) {
            ReadableByteStream input = bundle.get();
            WritableByteStream output = channel.getOutputStream();
            switch (state) {
                case CLIENT_LOGIN_STEP_1:
                    try {
                        byte[] array = new byte[input.getInt()];
                        input.get(array);
                        int keyLength = input.getInt();
                        keyLength = FastMath.min(keyLength, AES_MAX_KEY_LENGTH);
                        if (keyLength < AES_MIN_KEY_LENGTH) {
                            throw new IOException(
                                    "Key length too short: " + keyLength);
                        }
                        byte[] keyServer = new byte[keyLength];
                        byte[] keyClient = new byte[keyLength];
                        salt = new byte[SALT_LENGTH];
                        Random random = new SecureRandom();
                        random.nextBytes(keyServer);
                        random.nextBytes(keyClient);
                        random.nextBytes(salt);
                        output.putInt(keyLength);
                        PublicKey rsaKey = KeyFactory.getInstance("RSA")
                                .generatePublic(new X509EncodedKeySpec(array));
                        Cipher cipher = Cipher.getInstance("RSA");
                        cipher.init(Cipher.ENCRYPT_MODE, rsaKey);
                        output.put(cipher.update(keyServer));
                        output.put(cipher.update(keyClient));
                        output.put(cipher.doFinal(salt));
                        channel.queueBundle();
                        channel.setKey(keyClient, keyServer);
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidKeySpecException e) {
                        throw new IOException(e);
                    }
                    state = State.CLIENT_LOGIN_STEP_2;
                    break;
                case SERVER_LOGIN_STEP_1:
                    int keyLength = input.getInt();
                    keyLength = FastMath.min(keyLength, AES_MAX_KEY_LENGTH);
                    if (keyLength < AES_MIN_KEY_LENGTH) {
                        throw new IOException(
                                "Key length too short: " + keyLength);
                    }
                    byte[] keyServer = new byte[keyLength];
                    byte[] keyClient = new byte[keyLength];
                    byte[] salt = new byte[SALT_LENGTH];
                    try {
                        Cipher cipher = Cipher.getInstance("RSA");
                        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
                        byte[] array = new byte[cipher
                                .getOutputSize(keyLength << 1 + SALT_LENGTH)];
                        input.get(array);
                        array = cipher.doFinal(array);
                        System.arraycopy(array, 0, keyServer, 0, keyLength);
                        System.arraycopy(array, keyLength, keyClient, 0,
                                keyLength);
                        System.arraycopy(array, keyLength << 1, salt, 0,
                                SALT_LENGTH);
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                        throw new IOException(e);
                    }
                    channel.setKey(keyServer, keyClient);
                    challenge = new byte[1 << 10 << 2];
                    new SecureRandom().nextBytes(challenge);
                    try {
                        Cipher cipher = cipher(Cipher.ENCRYPT_MODE, salt);
                        output.putByteArrayLong(cipher.doFinal(challenge));
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        throw new IOException(e);
                    }
                    channel.queueBundle();
                    state = State.SERVER_LOGIN_STEP_2;
                    break;
                case CLIENT_LOGIN_STEP_2:
                    byte[] array = input.getByteArrayLong();
                    try {
                        Cipher cipher = cipher(Cipher.DECRYPT_MODE, this.salt);
                        this.salt = null;
                        output.put(cipher.doFinal(array));
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        throw new IOException(e);
                    }
                    channel.queueBundle();
                    state = State.OPEN;
                    break;
                case SERVER_LOGIN_STEP_2:
                    array = new byte[1 << 10 << 2];
                    input.get(array);
                    if (!Arrays.equals(array, challenge)) {
                        throw new ConnectionCloseException(
                                "Failed password authentication");
                    }
                    state = State.OPEN;
                    break;
                case OPEN:
                    int length = input.getInt();
                    String[] command = new String[length];
                    for (int i = 0; i < length; i++) {
                        command[i] = input.getString();
                    }
                    processCommand(command);
                    processing = true;
                    break;
            }
        }
        switch (state) {
            case OPEN:
                while (!queue.isEmpty()) {
                    String[] command = queue.poll();
                    WritableByteStream output = channel.getOutputStream();
                    output.putInt(command.length);
                    for (String str : command) {
                        output.putString(str);
                    }
                    channel.queueBundle();
                    processing = true;
                }
                break;
        }
        if (channel.process()) {
            return true;
        }
        if (state == State.CLOSING) {
            state = State.CLOSED;
        }
        return processing;
    }

    @Override
    public String toString() {
        return channel.toString();
    }

    enum State {
        CLIENT_LOGIN_STEP_1,
        CLIENT_LOGIN_STEP_2,
        SERVER_LOGIN_STEP_1,
        SERVER_LOGIN_STEP_2,
        OPEN,
        CLOSING,
        CLOSED
    }
}
