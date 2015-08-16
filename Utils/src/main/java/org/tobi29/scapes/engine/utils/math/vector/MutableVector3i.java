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

public class MutableVector3i extends MutableVector3 {
    private int x, y, z;

    public MutableVector3i() {
        this(0, 0, 0);
    }

    public MutableVector3i(Vector3 vector) {
        this(vector.intX(), vector.intY(), vector.intZ());
    }

    public MutableVector3i(MutableVector3 vector) {
        this(vector.intX(), vector.intY(), vector.intZ());
    }

    public MutableVector3i(int x, int y, int z) {
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
        x += vector.intX();
        y += vector.intY();
        return this;
    }

    @Override
    public MutableVector3 minus(Vector2 vector) {
        x -= vector.intX();
        y -= vector.intY();
        return this;
    }

    @Override
    public MutableVector3 multiply(Vector2 vector) {
        x *= vector.intX();
        y *= vector.intY();
        return this;
    }

    @Override
    public MutableVector3 div(Vector2 vector) {
        x /= vector.intX();
        y /= vector.intY();
        return this;
    }

    @Override
    public MutableVector3 set(Vector2 a) {
        x = a.intX();
        y = a.intY();
        return this;
    }

    @Override
    public MutableVector3 setX(int x) {
        this.x = x;
        return this;
    }

    @Override
    public MutableVector3 setX(long x) {
        this.x = (int) x;
        return this;
    }

    @Override
    public MutableVector3 setX(float x) {
        this.x = FastMath.floor(x);
        return this;
    }

    @Override
    public MutableVector3 setX(double x) {
        this.x = FastMath.floor(x);
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
        this.y = (int) y;
        return this;
    }

    @Override
    public MutableVector3 setY(float y) {
        this.y = FastMath.floor(y);
        return this;
    }

    @Override
    public MutableVector3 setY(double y) {
        this.y = FastMath.floor(y);
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
    public Vector3 now() {
        return new Vector3i(x, y, z);
    }

    @Override
    public MutableVector3 plus(Vector3 vector) {
        x += vector.intX();
        y += vector.intY();
        z += vector.intZ();
        return this;
    }

    @Override
    public MutableVector3 minus(Vector3 vector) {
        x -= vector.intX();
        y -= vector.intY();
        z -= vector.intZ();
        return this;
    }

    @Override
    public MutableVector3 multiply(Vector3 vector) {
        x *= vector.intX();
        y *= vector.intY();
        z *= vector.intZ();
        return this;
    }

    @Override
    public MutableVector3 div(Vector3 vector) {
        x /= vector.intX();
        y /= vector.intY();
        z /= vector.intZ();
        return this;
    }

    @Override
    public MutableVector3 set(Vector3 a) {
        setX(a.intX());
        setY(a.intY());
        setZ(a.intZ());
        return this;
    }

    @Override
    public MutableVector3 setZ(int z) {
        this.z = z;
        return this;
    }

    @Override
    public MutableVector3 setZ(long z) {
        this.z = (int) z;
        return this;
    }

    @Override
    public MutableVector3 setZ(float z) {
        this.z = FastMath.floor(z);
        return this;
    }

    @Override
    public MutableVector3 setZ(double z) {
        this.z = FastMath.floor(z);
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
    public TagStructure write() {
        TagStructure tagStructure = new TagStructure();
        tagStructure.setInteger("X", x);
        tagStructure.setInteger("Y", y);
        tagStructure.setInteger("Z", z);
        return tagStructure;
    }

    @Override
    public void read(TagStructure tagStructure) {
        x = tagStructure.getInteger("X");
        y = tagStructure.getInteger("Y");
        z = tagStructure.getInteger("Z");
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
