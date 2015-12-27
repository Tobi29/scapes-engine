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
package org.tobi29.scapes.engine.opengl.shader;

import java8.util.function.Consumer;
import org.tobi29.scapes.engine.utils.Streams;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderCompileInformation {
    private static final Pattern REPEAT =
            Pattern.compile("#repeat ([0-9]+) (.*)");
    private static final Pattern INDEX = Pattern.compile("\\$i");
    private final Map<String, Consumer<ShaderPreprocessor>>
            preCompileListeners = new ConcurrentHashMap<>();
    private final Map<String, Consumer<Shader>> postCompileListeners =
            new ConcurrentHashMap<>();
    
    public void supplyPreCompile(Consumer<ShaderPreprocessor> listener) {
        preCompileListeners.put("Oi", listener);
    }

    public void supplyPreCompile(String id,
            Consumer<ShaderPreprocessor> listener) {
        preCompileListeners.put(id, listener);
    }

    public void supplyPostCompile(String id, Consumer<Shader> listener) {
        postCompileListeners.put(id, listener);
    }

    public String processSource(String source) {
        Matcher matcher = REPEAT.matcher(source);
        StringBuffer buffer = new StringBuffer(source.length());
        while (matcher.find()) {
            int amount = 1;
            try {
                amount = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
            }
            String replacement = matcher.group(2) + '\n';
            StringBuilder loop =
                    new StringBuilder(amount * replacement.length());
            for (int i = 0; i < amount; i++) {
                loop.append(INDEX.matcher(replacement)
                        .replaceAll(String.valueOf(i)));
            }
            matcher.appendReplacement(buffer,
                    Matcher.quoteReplacement(loop.toString()));
        }
        matcher.appendTail(buffer);
        source = buffer.toString();
        return source;
    }

    public ShaderPreprocessor preCompile() {
        ShaderPreprocessor processor = new ShaderPreprocessor();
        Streams.of(preCompileListeners.values())
                .forEach(listener -> listener.accept(processor));
        return processor;
    }

    public void postCompile(Shader shader) {
        Streams.of(postCompileListeners.values())
                .forEach(listener -> listener.accept(shader));
    }
}
