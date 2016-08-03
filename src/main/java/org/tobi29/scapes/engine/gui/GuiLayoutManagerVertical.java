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

package org.tobi29.scapes.engine.gui;

import org.tobi29.scapes.engine.utils.Triple;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;
import org.tobi29.scapes.engine.utils.math.vector.Vector2;
import org.tobi29.scapes.engine.utils.math.vector.Vector2d;

import java.util.List;
import java.util.Set;

public class GuiLayoutManagerVertical extends GuiLayoutManager {
    public GuiLayoutManagerVertical(Vector2 start, Vector2 maxSize,
            Set<GuiComponent> components) {
        super(start, maxSize, components);
    }

    @Override
    protected void layout(List<Triple<GuiComponent, Vector2, Vector2>> output) {
        double unsized = 0.0;
        double usedHeight = 0.0;
        for (GuiComponent component : components) {
            GuiLayoutData data = component.parent;
            if (data instanceof GuiLayoutDataVertical) {
                GuiLayoutDataVertical dataVertical =
                        (GuiLayoutDataVertical) data;
                if (dataVertical.height() < 0.0) {
                    unsized -= dataVertical.height();
                } else {
                    Vector2 marginStart = dataVertical.marginStart();
                    Vector2 marginEnd = dataVertical.marginEnd();
                    usedHeight +=
                            dataVertical.height() + marginStart.doubleY() +
                                    marginEnd.doubleY();
                }
            }
        }
        MutableVector2 pos = new MutableVector2d();
        MutableVector2 size = new MutableVector2d();
        MutableVector2 offset = new MutableVector2d(start);
        MutableVector2 outSize = new MutableVector2d();
        Vector2 preferredSize = new Vector2d(maxSize.doubleX(),
                (maxSize.doubleY() - usedHeight) / unsized);
        for (GuiComponent component : components) {
            GuiLayoutData data = component.parent;
            pos.set(offset.now());
            size.set(data.width(), data.height());
            if (data instanceof GuiLayoutDataVertical) {
                GuiLayoutDataVertical dataVertical =
                        (GuiLayoutDataVertical) data;
                Vector2 marginStart = dataVertical.marginStart();
                Vector2 marginEnd = dataVertical.marginEnd();
                if (size.doubleX() >= 0.0) {
                    pos.plusX((maxSize.doubleX() - size.doubleX() -
                            marginStart.doubleX() - marginEnd.doubleX()) * 0.5);
                }
                size(size, preferredSize.minus(marginStart).minus(marginEnd),
                        maxSize.minus(marginStart).minus(marginEnd));
                pos.plus(marginStart);
                offset.plusY(size.doubleY() + marginStart.doubleY() +
                        marginEnd.doubleY());
                setSize(pos.now().plus(size.now()).plus(marginEnd), outSize);
            } else if (data instanceof GuiLayoutDataAbsolute) {
                GuiLayoutDataAbsolute dataAbsolute =
                        (GuiLayoutDataAbsolute) data;
                pos.set(dataAbsolute.pos());
                size(size, preferredSize, maxSize);
                setSize(pos.now().plus(size.now()), outSize);
            } else {
                throw new IllegalStateException(
                        "Invalid layout node: " + data.getClass());
            }
            output.add(new Triple<>(component, pos.now(), size.now()));
        }
        this.size = outSize.now();
    }
}
