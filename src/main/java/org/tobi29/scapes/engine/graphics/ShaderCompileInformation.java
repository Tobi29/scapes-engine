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

package org.tobi29.scapes.engine.graphics;

import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import org.tobi29.scapes.engine.utils.Streams;

import java.util.ArrayList;
import java.util.List;

public class ShaderCompileInformation {
    private final List<BiConsumer<GL, ShaderPreprocessor>> preCompileListeners =
            new ArrayList<>();
    private final List<BiConsumer<GL, Shader>> postCompileListeners =
            new ArrayList<>();

    public void supplyPreCompile(Consumer<ShaderPreprocessor> listener) {
        supplyPreCompile((gl, shader) -> listener.accept(shader));
    }

    public void supplyPreCompile(BiConsumer<GL, ShaderPreprocessor> listener) {
        preCompileListeners.add(listener);
    }

    public void supplyPostCompile(Consumer<Shader> listener) {
        supplyPostCompile((gl, shader) -> listener.accept(shader));
    }

    public void supplyPostCompile(BiConsumer<GL, Shader> listener) {
        postCompileListeners.add(listener);
    }

    public ShaderPreprocessor preCompile(GL gl) {
        ShaderPreprocessor processor = new ShaderPreprocessor();
        processor.supplyProperty("SCENE_WIDTH", gl.sceneWidth());
        processor.supplyProperty("SCENE_HEIGHT", gl.sceneHeight());
        processor.supplyProperty("CONTAINER_WIDTH", gl.containerWidth());
        processor.supplyProperty("CONTAINER_HEIGHT", gl.containerHeight());
        processor.supplyProperty("CONTENT_WIDTH", gl.contentWidth());
        processor.supplyProperty("CONTENT_HEIGHT", gl.contentHeight());
        Streams.forEach(preCompileListeners,
                listener -> listener.accept(gl, processor));
        return processor;
    }

    public void postCompile(GL gl, Shader shader) {
        Streams.forEach(postCompileListeners,
                listener -> listener.accept(gl, shader));
    }
}
