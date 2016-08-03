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

package org.tobi29.scapes.engine.server;

import java8.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.utils.io.IOConsumer;
import org.tobi29.scapes.engine.utils.io.RandomReadableByteStream;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.nio.channels.Selector;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ControlPanelProtocol implements Connection {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ControlPanelProtocol.class);
    private static final int CHALLENGE_LENGTH = 1 << 10 << 2, SALT_LENGTH = 8,
            CHALLENGE_CIPHER_LENGTH = CHALLENGE_LENGTH + SALT_LENGTH;
    private final PacketBundleChannel channel;
    private final Queue<String[]> queue = new ConcurrentLinkedQueue<>();
    private final Queue<Runnable> closeHooks = new ConcurrentLinkedQueue<>();
    private final Map<String, IOConsumer<String[]>> commands =
            new ConcurrentHashMap<>();
    private final String password;
    private State state;
    private byte[] challenge, salt = new byte[SALT_LENGTH];

    public ControlPanelProtocol(PacketBundleChannel channel, boolean client,
            String password) throws IOException {
        this.channel = channel;
        this.password = password;
        addCommand("Core:End", command -> {
            throw new ConnectionCloseException("Remote connection end");
        });
        addCommand("Core:CommandsList", command -> {
            Set<String> set = commands.keySet();
            send("Core:CommandsSend", set.toArray(new String[set.size()]));
        });
        if (client) {
            state = State.CLIENT_LOGIN;
        } else {
            challenge = new byte[CHALLENGE_LENGTH];
            new SecureRandom().nextBytes(challenge);
            salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);
            WritableByteStream output = this.channel.getOutputStream();
            try {
                Cipher cipher = cipher(Cipher.ENCRYPT_MODE, salt);
                output.put(cipher.doFinal(challenge));
                output.put(salt);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new IOException(e);
            }
            this.channel.queueBundle();
            state = State.SERVER_LOGIN;
        }
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

    public void closeHook(Runnable runnable) {
        closeHooks.add(runnable);
    }

    @Override
    public void register(Selector selector, int opt) throws IOException {
        channel.register(selector, opt);
    }

    @SuppressWarnings("UnnecessaryToStringCall")
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
    public void requestClose() {
        channel.requestClose();
    }

    @Override
    public void close() throws IOException {
        while (!closeHooks.isEmpty()) {
            closeHooks.poll().run();
        }
        channel.close();
    }

    public boolean tick() throws IOException {
        if (state == State.CLOSED) {
            return false;
        }
        boolean processing = false;
        Optional<RandomReadableByteStream> bundle = channel.fetch();
        if (bundle.isPresent()) {
            ReadableByteStream input = bundle.get();
            WritableByteStream output = channel.getOutputStream();
            switch (state) {
                case CLIENT_LOGIN:
                    byte[] challenge = new byte[CHALLENGE_CIPHER_LENGTH];
                    byte[] salt = new byte[SALT_LENGTH];
                    input.get(challenge);
                    input.get(salt);
                    try {
                        Cipher cipher = cipher(Cipher.DECRYPT_MODE, salt);
                        output.put(cipher.doFinal(challenge));
                    } catch (IllegalBlockSizeException | BadPaddingException e) {
                        throw new IOException(e);
                    }
                    channel.queueBundle();
                    state = State.OPEN;
                    break;
                case SERVER_LOGIN:
                    byte[] check = new byte[CHALLENGE_LENGTH];
                    input.get(check);
                    if (!Arrays.equals(check, this.challenge)) {
                        throw new ConnectionCloseException(
                                "Failed password authentication");
                    }
                    this.challenge = null;
                    this.salt = null;
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
            state = State.CLOSED;
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
        CLIENT_LOGIN,
        SERVER_LOGIN,
        OPEN,
        CLOSING,
        CLOSED
    }
}
