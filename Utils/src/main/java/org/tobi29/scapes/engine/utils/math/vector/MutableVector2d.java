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

public class MutableVector2d extends MutableVector2 {
    private double x, y;

    public MutableVector2d() {
        this(0.0, 0.0);
    }

    public MutableVector2d(Vector2 vector) {
        this(vector.doubleX(), vector.doubleY());
    }

    public MutableVector2d(MutableVector2 vector) {
        this(vector.doubleX(), vector.doubleY());
    }

    public MutableVector2d(double x, double y) {
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
        x += vector.doubleX();
        y += vector.doubleY();
        return this;
    }

    @Override
    public MutableVector2 minus(Vector2 vector) {
        x -= vector.doubleX();
        y -= vector.doubleY();
        return this;
    }

    @Override
    public MutableVector2 multiply(Vector2 vector) {
        x *= vector.doubleX();
        y *= vector.doubleY();
        return this;
    }

    @Override
    public MutableVector2 div(Vector2 vector) {
        x /= vector.doubleX();
        y /= vector.doubleY();
        return this;
    }

    @Override
    public MutableVector2 set(Vector2 a) {
        setX(a.doubleX());
        setY(a.doubleY());
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
        this.x = x;
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
        this.y = y;
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
        return (float) x;
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
        return (float) y;
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
        long temp;
        temp = Double.doubleToLongBits(x);
        int result = (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ temp >>> 32);
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
            return x == other.doubleX() && y == other.doubleY();
        }
        if (!(obj instanceof Vector2)) {
            return false;
        }
        Vector2 other = (Vector2) obj;
        return x == other.doubleX() && y == other.doubleY();
    }

    @Override
    public TagStructure write() {
        TagStructure tagStructure = new TagStructure();
        tagStructure.setDouble("X", x);
        tagStructure.setDouble("Y", y);
        return tagStructure;
    }

    @Override
    public void read(TagStructure tagStructure) {
        x = tagStructure.getDouble("X");
        y = tagStructure.getDouble("Y");
    }
}
