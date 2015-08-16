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

package org.tobi29.scapes.engine.utils.math;

import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3i;

public enum Face {
    NONE(-1, 0, 0, 1, (byte) -1),
    UP(0, 0, 0, 1, (byte) 0),
    DOWN(1, 0, 0, -1, (byte) 1),
    NORTH(2, 0, -1, 0, (byte) 2),
    EAST(3, 1, 0, 0, (byte) 3),
    SOUTH(4, 0, 1, 0, (byte) 4),
    WEST(5, -1, 0, 0, (byte) 5);

    static {
        NONE.opposite = NONE;
        UP.opposite = DOWN;
        DOWN.opposite = UP;
        NORTH.opposite = SOUTH;
        EAST.opposite = WEST;
        SOUTH.opposite = NORTH;
        WEST.opposite = EAST;
    }

    private final int value;
    private final int x, y, z;
    private final Vector3 delta;
    private final byte data;
    private Face opposite;

    Face(int value, int x, int y, int z, byte data) {
        this.value = value;
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
        delta = new Vector3i(x, y, z);
    }

    public static Face get(int data) {
        switch (data) {
            case 0:
                return UP;
            case 1:
                return DOWN;
            case 2:
                return NORTH;
            case 3:
                return EAST;
            case 4:
                return SOUTH;
            case 5:
                return WEST;
            default:
                return NONE;
        }
    }

    public int getValue() {
        return value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Vector3 getDelta() {
        return delta;
    }

    public byte getData() {
        return data;
    }

    public Face getOpposite() {
        return opposite;
    }
}
