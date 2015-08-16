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

package org.tobi29.scapes.engine.backends.lwjgl3.glfw;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.Container;
import org.tobi29.scapes.engine.spi.ScapesEngineBackendProvider;

public class GLFWBackendProvider implements ScapesEngineBackendProvider {
    @Override
    public boolean available() {
        return true;
    }

    @Override
    public Container createContainer(ScapesEngine engine) {
        return new ContainerGLFW(engine);
    }
}
