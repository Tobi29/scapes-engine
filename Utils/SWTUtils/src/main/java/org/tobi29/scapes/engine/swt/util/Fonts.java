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

package org.tobi29.scapes.engine.swt.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

public final class Fonts {
    private static final Logger LOGGER = LoggerFactory.getLogger(Fonts.class);
    private static final Pattern SPLIT_SEMICOLON = Pattern.compile(";");
    private static final Pattern SPLIT_BAR = Pattern.compile("\\|");
    private static final Pattern SPLIT_COMMA = Pattern.compile(",");
    private static final FontData[] MONOSPACED = fontDatas("Monospaced");

    private Fonts() {
    }

    public static FontData[] monospace() {
        return Arrays.copyOf(MONOSPACED, MONOSPACED.length);
    }

    private static FontData[] fontDatas(String font) {
        try {
            ClassLoader classLoader = Fonts.class.getClassLoader();
            Properties properties = new Properties();
            try (InputStream streamIn = classLoader
                    .getResourceAsStream("fonts/" + font + ".properties")) {
                properties.load(streamIn);
            }
            String os = identifier(System.getProperty("os.name"));
            String ws = identifier(SWT.getPlatform());
            String fonts = properties.getProperty(os + '_' + ws);
            if (fonts == null) {
                fonts = properties.getProperty(os);
            }
            if (fonts == null) {
                LOGGER.warn("Unable to identify OS, using fallback.");
                fonts = properties.getProperty("unknown");
            }
            String[] fontDataTexts = SPLIT_SEMICOLON.split(fonts);
            FontData[] fontDatas = new FontData[fontDataTexts.length];
            for (int i = 0; i < fontDatas.length; i++) {
                String[] split = SPLIT_BAR.split(fontDataTexts[i], 3);
                assert split.length == 3;
                String name = split[0];
                String[] styles = SPLIT_COMMA.split(split[1]);
                int style = 0;
                for (String s : styles) {
                    switch (s) {
                        case "normal":
                            style |= SWT.NORMAL;
                            break;
                        case "bold":
                            style |= SWT.BOLD;
                            break;
                        case "italic":
                            style |= SWT.ITALIC;
                            break;
                    }
                }
                int height = Integer.parseInt(split[2]);
                fontDatas[i] = new FontData(name, height, style);
            }
            return fontDatas;
        } catch (IOException e) {
            LOGGER.error("Failed to load font: {}", e.toString());
        }
        return null;
    }

    private static String identifier(String str) {
        char[] characters = new char[str.length()];
        int i = 0;
        for (int j = 0; j < str.length(); j++) {
            char character = str.charAt(j);
            if (!Character.isWhitespace(character)) {
                characters[i++] = Character.toLowerCase(character);
            }
        }
        return new String(characters, 0, i);
    }
}
