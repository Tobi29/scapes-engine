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

package org.tobi29.scapes.engine;

import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.utils.VersionUtil;
import org.tobi29.scapes.engine.utils.graphics.Image;

public abstract class Game {
    protected ScapesEngine engine;

    public abstract String name();

    public abstract String id();

    public abstract VersionUtil.Version version();

    public abstract Image icon();

    public abstract void init();

    public abstract void initLate(GL gl);

    public abstract void step();

    public abstract void dispose();
}
