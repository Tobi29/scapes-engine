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

public class MutableVector2f extends MutableVector2 {
    private float x, y;

    public MutableVector2f() {
        this(0.0f, 0.0f);
    }

    public MutableVector2f(Vector2 vector) {
        this(vector.floatX(), vector.floatY());
    }

    public MutableVector2f(MutableVector2 vector) {
        this(vector.floatX(), vector.floatY());
    }

    public MutableVector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public MutableVector2 plus(int a) {
        x += a;
        y += a;
        return this;
    }

    @Override
    public MutableVector2 plus(long a) {
        x += a;
        y += a;
        return this;
    }

    @Override
    public MutableVector2 plus(float a) {
        x += a;
        y += a;
        return this;
    }

    @Override
    public MutableVector2 plus(double a) {
        x += a;
        y += a;
        return this;
    }

    @Override
    public MutableVector2 minus(int a) {
        x -= a;
        y -= a;
        return this;
    }

    @Override
    public MutableVector2 minus(long a) {
        x -= a;
        y -= a;
        return this;
    }

    @Override
    public MutableVector2 minus(float a) {
        x -= a;
        y -= a;
        return this;
    }

    @Override
    public MutableVector2 minus(double a) {
        x -= a;
        y -= a;
        return this;
    }

    @Override
    public MutableVector2 multiply(int a) {
        x *= a;
        y *= a;
        return this;
    }

    @Override
    public MutableVector2 multiply(long a) {
        x *= a;
        y *= a;
        return this;
    }

    @Override
    public MutableVector2 multiply(float a) {
        x *= a;
        y *= a;
        return this;
    }

    @Override
    public MutableVector2 multiply(double a) {
        x *= a;
        y *= a;
        return this;
    }

    @Override
    public MutableVector2 div(int a) {
        x /= a;
        y /= a;
        return this;
    }

    @Override
    public MutableVector2 div(long a) {
        x /= a;
        y /= a;
        return this;
    }

    @Override
    public MutableVector2 div(float a) {
        x /= a;
        y /= a;
        return this;
    }

    @Override
    public MutableVector2 div(double a) {
        x /= a;
        y /= a;
        return this;
    }

    @Override
    public MutableVector2 plus(Vector2 vector) {
        x += vector.floatX();
        y += vector.floatY();
        return this;
    }

    @Override
    public MutableVector2 minus(Vector2 vector) {
        x -= vector.floatX();
        y -= vector.floatY();
        return this;
    }

    @Override
    public MutableVector2 multiply(Vector2 vector) {
        x *= vector.floatX();
        y *= vector.floatY();
        return this;
    }

    @Override
    public MutableVector2 div(Vector2 vector) {
        x /= vector.floatX();
        y /= vector.floatY();
        return this;
    }

    @Override
    public MutableVector2 set(Vector2 a) {
        setX(a.floatX());
        setY(a.floatY());
        return this;
    }

    @Override
    public MutableVector2 setX(int x) {
        this.x = x;
        return this;
    }

    @Override
    public MutableVector2 setX(long x) {
        this.x = (int) x;
        return this;
    }

    @Override
    public MutableVector2 setX(float x) {
        this.x = x;
        return this;
    }

    @Override
    public MutableVector2 setX(double x) {
        this.x = (float) x;
        return this;
    }

    @Override
    public MutableVector2 plusX(int x) {
        this.x += x;
        return this;
    }

    @Override
    public MutableVector2 plusX(long x) {
        this.x += x;
        return this;
    }

    @Override
    public MutableVector2 plusX(float x) {
        this.x += x;
        return this;
    }

    @Override
    public MutableVector2 plusX(double x) {
        this.x += x;
        return this;
    }

    @Override
    public MutableVector2 setY(int y) {
        this.y = y;
        return this;
    }

    @Override
    public MutableVector2 setY(long y) {
        this.y = (int) y;
        return this;
    }

    @Override
    public MutableVector2 setY(float y) {
        this.y = y;
        return this;
    }

    @Override
    public MutableVector2 setY(double y) {
        this.y = (float) y;
        return this;
    }

    @Override
    public MutableVector2 plusY(int y) {
        this.y += y;
        return this;
    }

    @Override
    public MutableVector2 plusY(long y) {
        this.y += y;
        return this;
    }

    @Override
    public MutableVector2 plusY(float y) {
        this.y += y;
        return this;
    }

    @Override
    public MutableVector2 plusY(double y) {
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
        return FastMath.floor(x);
    }

    @Override
    public long longY() {
        return FastMath.floor(x);
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
    public Vector2 now() {
        return new Vector2d(x, y);
    }

    @Override
    public int hashCode() {
        int result = x == 0.0f ? 0 : Float.floatToIntBits(x);
        result = 31 * result + (y == 0.0f ? 0 : Float.floatToIntBits(y));
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
        if (obj instanceof MutableVector2) {
            MutableVector2 other = (MutableVector2) obj;
            return x == other.floatX() && y == other.floatY();
        }
        if (!(obj instanceof Vector2)) {
            return false;
        }
        Vector2 other = (Vector2) obj;
        return x == other.floatX() && y == other.floatY();
    }

    @Override
    public TagStructure write() {
        TagStructure tagStructure = new TagStructure();
        tagStructure.setFloat("X", x);
        tagStructure.setFloat("Y", y);
        return tagStructure;
    }

    @Override
    public void read(TagStructure tagStructure) {
        x = tagStructure.getFloat("X");
        y = tagStructure.getFloat("Y");
    }
}
