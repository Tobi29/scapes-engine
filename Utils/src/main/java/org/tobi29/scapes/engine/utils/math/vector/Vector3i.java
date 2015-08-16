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

package org.tobi29.scapes.engine.utils.math.vector;

import org.tobi29.scapes.engine.utils.math.FastMath;

public class Vector3i extends Vector3 {
    public static final Vector3i ZERO = new Vector3i(0, 0, 0);
    private final int x, y, z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Vector3 plus(int a) {
        return new Vector3i(x + a, y + a, z + a);
    }

    @Override
    public Vector3 plus(long a) {
        return new Vector3i((int) (x + a), (int) (y + a), (int) (z + a));
    }

    @Override
    public Vector3 plus(float a) {
        return new Vector3i(FastMath.floor(x + a), FastMath.floor(y + a),
                FastMath.floor(z + a));
    }

    @Override
    public Vector3 plus(double a) {
        return new Vector3i(FastMath.floor(x + a), FastMath.floor(y + a),
                FastMath.floor(z + a));
    }

    @Override
    public Vector3 minus(int a) {
        return new Vector3i(x - a, y - a, z - a);
    }

    @Override
    public Vector3 minus(long a) {
        return new Vector3i((int) (x - a), (int) (y - a), (int) (z - a));
    }

    @Override
    public Vector3 minus(float a) {
        return new Vector3i(FastMath.floor(x - a), FastMath.floor(y - a),
                FastMath.floor(z - a));
    }

    @Override
    public Vector3 minus(double a) {
        return new Vector3i(FastMath.floor(x - a), FastMath.floor(y - a),
                FastMath.floor(z - a));
    }

    @Override
    public Vector3 multiply(int a) {
        return new Vector3i(x * a, y * a, z * a);
    }

    @Override
    public Vector3 multiply(long a) {
        return new Vector3i(FastMath.floor(x * a), FastMath.floor(y * a),
                FastMath.floor(z * a));
    }

    @Override
    public Vector3 multiply(float a) {
        return new Vector3i(FastMath.floor(x * a), FastMath.floor(y * a),
                FastMath.floor(z * a));
    }

    @Override
    public Vector3 multiply(double a) {
        return new Vector3i(FastMath.floor(x * a), FastMath.floor(y * a),
                FastMath.floor(z * a));
    }

    @Override
    public Vector3 div(int a) {
        return new Vector3i(x / a, y / a, z / a);
    }

    @Override
    public Vector3 div(long a) {
        return new Vector3i((int) (x / a), (int) (y / a), (int) (z / a));
    }

    @Override
    public Vector3 div(float a) {
        return new Vector3i(FastMath.floor(x / a), FastMath.floor(y / a),
                FastMath.floor(z / a));
    }

    @Override
    public Vector3 div(double a) {
        return new Vector3i(FastMath.floor(x / a), FastMath.floor(y / a),
                FastMath.floor(z / a));
    }

    @Override
    public Vector3 plus(Vector3 vector) {
        return new Vector3i(x + vector.intX(), y + vector.intY(),
                z + vector.intZ());
    }

    @Override
    public Vector3 minus(Vector3 vector) {
        return new Vector3i(x - vector.intX(), y - vector.intY(),
                z - vector.intZ());
    }

    @Override
    public Vector3 multiply(Vector3 vector) {
        return new Vector3i(x * vector.intX(), y * vector.intY(),
                z * vector.intZ());
    }

    @Override
    public Vector3 div(Vector3 vector) {
        return new Vector3i(x / vector.intX(), y / vector.intY(),
                z / vector.intZ());
    }

    @Override
    public int intZ() {
        return z;
    }

    @Override
    public long longZ() {
        return z;
    }

    @Override
    public float floatZ() {
        return z;
    }

    @Override
    public double doubleZ() {
        return z;
    }

    @Override
    public Vector3 plus(Vector2 vector) {
        return new Vector3i(x + vector.intX(), y + vector.intY(), z);
    }

    @Override
    public Vector3 minus(Vector2 vector) {
        return new Vector3i(x - vector.intX(), y - vector.intY(), z);
    }

    @Override
    public Vector3 multiply(Vector2 vector) {
        return new Vector3i(x * vector.intX(), y * vector.intY(), z);
    }

    @Override
    public Vector3 div(Vector2 vector) {
        return new Vector3i(x / vector.intX(), y / vector.intY(), z);
    }

    @Override
    public int intX() {
        return x;
    }

    @Override
    public long longX() {
        return x;
    }

    @Override
    public float floatX() {
        return x;
    }

    @Override
    public double doubleX() {
        return x;
    }

    @Override
    public int intY() {
        return y;
    }

    @Override
    public long longY() {
        return y;
    }

    @Override
    public float floatY() {
        return y;
    }

    @Override
    public double doubleY() {
        return y;
    }

    @Override
    public boolean hasNaN() {
        return false;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof MutableVector3) {
            MutableVector3 other = (MutableVector3) obj;
            return x == other.intX() && y == other.intY() && z == other.intZ();
        }
        if (!(obj instanceof Vector3)) {
            return false;
        }
        Vector3 other = (Vector3) obj;
        return x == other.intX() && y == other.intY() && z == other.intZ();
    }
}
