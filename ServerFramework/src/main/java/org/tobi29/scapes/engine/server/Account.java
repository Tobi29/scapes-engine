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

import java8.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.utils.SSLUtil;
import org.tobi29.scapes.engine.utils.UnsupportedJVMException;
import org.tobi29.scapes.engine.utils.io.ByteStreamInputStream;
import org.tobi29.scapes.engine.utils.io.ByteStreamOutputStream;
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath;
import org.tobi29.scapes.engine.utils.io.filesystem.FileUtil;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

public class Account {
    private static final Logger LOGGER = LoggerFactory.getLogger(Account.class);
    private final KeyPair keyPair;
    private final String nickname;

    public Account(KeyPair keyPair, String nickname) {
        this.keyPair = keyPair;
        this.nickname = nickname;
    }

    public static Account read(FilePath path) throws IOException {
        String key = null, nickname = "";
        if (FileUtil.exists(path)) {
            Properties properties = new Properties();
            FileUtil.read(path, stream -> properties
                    .load(new ByteStreamInputStream(stream)));
            key = properties.getProperty("Key");
            nickname = properties.getProperty("Nickname", "");
        }
        Optional<KeyPair> keyPair = key(key);
        if (keyPair.isPresent()) {
            return new Account(keyPair.get(), nickname);
        }
        Account account = new Account(genKey(), nickname);
        account.write(path);
        return account;
    }

    @SuppressWarnings("UnnecessaryToStringCall")
    public static Optional<KeyPair> key(String str) {
        if (str == null) {
            return Optional.empty();
        }
        try {
            RSAPrivateKey privateKey = SSLUtil.readPrivate(str);
            PublicKey publicKey = SSLUtil.extractPublic(privateKey);
            return Optional.of(new KeyPair(publicKey, privateKey));
        } catch (InvalidKeySpecException | IllegalArgumentException e) {
            LOGGER.warn("Failed to parse key: {}", e.toString());
            return Optional.empty();
        }
    }

    public static KeyPair genKey() {
        LOGGER.info("Generating key-pair...");
        try {
            KeyPairGenerator keyPairGenerator =
                    KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096);
            return keyPairGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedJVMException(e);
        }
    }

    public static String key(KeyPair keyPair) {
        if (keyPair == null) {
            return "";
        }
        try {
            return SSLUtil.writePrivate(keyPair.getPrivate());
        } catch (InvalidKeySpecException e) {
            return "";
        }
    }

    public static boolean valid(String nickname) {
        return !isNameValid(nickname).isPresent();
    }

    public static Optional<String> isNameValid(String nickname) {
        if (nickname.length() < 6) {
            return Optional.of("Name must be at least 6 characters long!");
        }
        if (nickname.length() > 20) {
            return Optional.of("Name may not be longer than 20 characters!");
        }
        if (nickname.contains(" ")) {
            return Optional.of("Name may not contain spaces!");
        }
        for (int i = 0; i < nickname.length(); i++) {
            if (!Character.isLetterOrDigit(nickname.charAt(i))) {
                return Optional.of("Name may only contain letters and digits!");
            }
        }
        return Optional.empty();
    }

    public void write(FilePath path) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("Key", key(keyPair));
        properties.setProperty("Nickname", nickname);
        FileUtil.write(path, stream -> properties
                .store(new ByteStreamOutputStream(stream), ""));
    }

    public KeyPair keyPair() {
        return keyPair;
    }

    public String nickname() {
        return nickname;
    }

    public boolean valid() {
        return valid(nickname);
    }
}
