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

import org.tobi29.scapes.engine.utils.io.tag.TagStructure;
import org.tobi29.scapes.engine.utils.math.FastMath;

public class MutableVector3f extends MutableVector3 {
    private float x, y, z;

    public MutableVector3f() {
        this(0.0f, 0.0f, 0.0f);
    }

    public MutableVector3f(Vector3 vector) {
        this(vector.floatX(), vector.floatY(), vector.floatZ());
    }

    public MutableVector3f(MutableVector3 vector) {
        this(vector.floatX(), vector.floatY(), vector.floatZ());
    }

    public MutableVector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public MutableVector3 plus(int a) {
        x += a;
        y += a;
        z += a;
        return this;
    }

    @Override
    public MutableVector3 plus(long a) {
        x += a;
        y += a;
        z += a;
        return this;
    }

    @Override
    public MutableVector3 plus(float a) {
        x += a;
        y += a;
        z += a;
        return this;
    }

    @Override
    public MutableVector3 plus(double a) {
        x += a;
        y += a;
        z += a;
        return this;
    }

    @Override
    public MutableVector3 minus(int a) {
        x -= a;
        y -= a;
        z -= a;
        return this;
    }

    @Override
    public MutableVector3 minus(long a) {
        x -= a;
        y -= a;
        z -= a;
        return this;
    }

    @Override
    public MutableVector3 minus(float a) {
        x -= a;
        y -= a;
        z -= a;
        return this;
    }

    @Override
    public MutableVector3 minus(double a) {
        x -= a;
        y -= a;
        z -= a;
        return this;
    }

    @Override
    public MutableVector3 multiply(int a) {
        x *= a;
        y *= a;
        z *= a;
        return this;
    }

    @Override
    public MutableVector3 multiply(long a) {
        x *= a;
        y *= a;
        z *= a;
        return this;
    }

    @Override
    public MutableVector3 multiply(float a) {
        x *= a;
        y *= a;
        z *= a;
        return this;
    }

    @Override
    public MutableVector3 multiply(double a) {
        x *= a;
        y *= a;
        z *= a;
        return this;
    }

    @Override
    public MutableVector3 div(int a) {
        x /= a;
        y /= a;
        z /= a;
        return this;
    }

    @Override
    public MutableVector3 div(long a) {
        x /= a;
        y /= a;
        z /= a;
        return this;
    }

    @Override
    public MutableVector3 div(float a) {
        x /= a;
        y /= a;
        z /= a;
        return this;
    }

    @Override
    public MutableVector3 div(double a) {
        x /= a;
        y /= a;
        z /= a;
        return this;
    }

    @Override
    public MutableVector3 plus(Vector2 vector) {
        x += vector.floatX();
        y += vector.floatY();
        return this;
    }

    @Override
    public MutableVector3 minus(Vector2 vector) {
        x -= vector.floatX();
        y -= vector.floatY();
        return this;
    }

    @Override
    public MutableVector3 multiply(Vector2 vector) {
        x *= vector.floatX();
        y *= vector.floatY();
        return this;
    }

    @Override
    public MutableVector3 div(Vector2 vector) {
        x /= vector.floatX();
        y /= vector.floatY();
        return this;
    }

    @Override
    public MutableVector3 set(Vector2 a) {
        x = a.floatX();
        y = a.floatY();
        return this;
    }

    @Override
    public MutableVector3 setX(int x) {
        this.x = x;
        return this;
    }

    @Override
    public MutableVector3 setX(long x) {
        this.x = x;
        return this;
    }

    @Override
    public MutableVector3 setX(float x) {
        this.x = x;
        return this;
    }

    @Override
    public MutableVector3 setX(double x) {
        this.x = (float) x;
        return this;
    }

    @Override
    public MutableVector3 plusX(int x) {
        this.x += x;
        return this;
    }

    @Override
    public MutableVector3 plusX(long x) {
        this.x += x;
        return this;
    }

    @Override
    public MutableVector3 plusX(float x) {
        this.x += x;
        return this;
    }

    @Override
    public MutableVector3 plusX(double x) {
        this.x += x;
        return this;
    }

    @Override
    public MutableVector3 setY(int y) {
        this.y = y;
        return this;
    }

    @Override
    public MutableVector3 setY(long y) {
        this.y = y;
        return this;
    }

    @Override
    public MutableVector3 setY(float y) {
        this.y = y;
        return this;
    }

    @Override
    public MutableVector3 setY(double y) {
        this.y = (float) y;
        return this;
    }

    @Override
    public MutableVector3 plusY(int y) {
        this.y += y;
        return this;
    }

    @Override
    public MutableVector3 plusY(long y) {
        this.y += y;
        return this;
    }

    @Override
    public MutableVector3 plusY(float y) {
        this.y += y;
        return this;
    }

    @Override
    public MutableVector3 plusY(double y) {
        this.y += y;
        return this;
    }

    @Override
    public int intX() {
        return FastMath.floor(x);
    }

    @Override
    public long longX() {
        return FastMath.floor(x);
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
        return FastMath.floor(y);
    }

    @Override
    public long longY() {
        return FastMath.floor(y);
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
    public Vector3 now() {
        return new Vector3d(x, y, z);
    }

    @Override
    public MutableVector3 plus(Vector3 vector) {
        x += vector.floatX();
        y += vector.floatY();
        z += vector.floatZ();
        return this;
    }

    @Override
    public MutableVector3 minus(Vector3 vector) {
        x -= vector.floatX();
        y -= vector.floatY();
        z -= vector.floatZ();
        return this;
    }

    @Override
    public MutableVector3 multiply(Vector3 vector) {
        x *= vector.floatX();
        y *= vector.floatY();
        z *= vector.floatZ();
        return this;
    }

    @Override
    public MutableVector3 div(Vector3 vector) {
        x /= vector.floatX();
        y /= vector.floatY();
        z /= vector.floatZ();
        return this;
    }

    @Override
    public MutableVector3 set(Vector3 a) {
        setX(a.floatX());
        setY(a.floatY());
        setZ(a.floatZ());
        return this;
    }

    @Override
    public MutableVector3 setZ(int z) {
        this.z = z;
        return this;
    }

    @Override
    public MutableVector3 setZ(long z) {
        this.z = z;
        return this;
    }

    @Override
    public MutableVector3 setZ(float z) {
        this.z = z;
        return this;
    }

    @Override
    public MutableVector3 setZ(double z) {
        this.z = (float) z;
        return this;
    }

    @Override
    public MutableVector3 plusZ(int z) {
        this.z += z;
        return this;
    }

    @Override
    public MutableVector3 plusZ(long z) {
        this.z += z;
        return this;
    }

    @Override
    public MutableVector3 plusZ(float z) {
        this.z += z;
        return this;
    }

    @Override
    public MutableVector3 plusZ(double z) {
        this.z += z;
        return this;
    }

    @Override
    public int intZ() {
        return FastMath.floor(z);
    }

    @Override
    public long longZ() {
        return FastMath.floor(z);
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
    public TagStructure write() {
        TagStructure tagStructure = new TagStructure();
        tagStructure.setFloat("X", x);
        tagStructure.setFloat("Y", y);
        tagStructure.setFloat("Z", z);
        return tagStructure;
    }

    @Override
    public void read(TagStructure tagStructure) {
        x = tagStructure.getFloat("X");
        y = tagStructure.getFloat("Y");
        z = tagStructure.getFloat("Z");
    }

    @Override
    public int hashCode() {
        int result = x == 0.0f ? 0 : Float.floatToIntBits(x);
        result = 31 * result + (y == 0.0f ? 0 : Float.floatToIntBits(y));
        result = 31 * result + (z == 0.0f ? 0 : Float.floatToIntBits(z));
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
            return x == other.floatX() && y == other.floatY() &&
                    z == other.floatZ();
        }
        if (!(obj instanceof Vector3)) {
            return false;
        }
        Vector3 other = (Vector3) obj;
        return x == other.floatX() && y == other.floatY() &&
                z == other.floatZ();
    }
}
