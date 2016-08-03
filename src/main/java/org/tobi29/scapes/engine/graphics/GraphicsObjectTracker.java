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

import java.util.ArrayList;
import java.util.List;

public class GraphicsObjectTracker<O extends GraphicsObject> {
    private final List<O> objects = new ArrayList<>();
    private int disposeOffset;

    public void disposeUnused(GL gl) {
        long time = System.currentTimeMillis();
        for (int i = disposeOffset; i < objects.size(); i += 16) {
            O object = objects.get(i);
            assert object.isStored();
            if (!object.isUsed(time)) {
                object.dispose(gl);
                object.reset();
            }
        }
        disposeOffset++;
        disposeOffset &= 15;
    }

    public void disposeAll(GL gl) {
        while (!objects.isEmpty()) {
            O object = objects.get(0);
            object.dispose(gl);
            object.reset();
        }
        objects.clear();
    }

    public void resetAll() {
        while (!objects.isEmpty()) {
            O object = objects.get(0);
            object.reset();
        }
        objects.clear();
    }

    public int count() {
        return objects.size();
    }

    public Runnable attach(O object) {
        objects.add(object);
        return () -> objects.remove(object);
    }
}
