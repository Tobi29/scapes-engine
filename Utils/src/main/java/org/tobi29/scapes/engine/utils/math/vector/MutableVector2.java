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

import org.tobi29.scapes.engine.utils.io.tag.MultiTag;

public abstract class MutableVector2 implements MultiTag.ReadAndWrite {
    public abstract MutableVector2 plus(int a);

    public abstract MutableVector2 plus(long a);

    public abstract MutableVector2 plus(float a);

    public abstract MutableVector2 plus(double a);

    public abstract MutableVector2 minus(int a);

    public abstract MutableVector2 minus(long a);

    public abstract MutableVector2 minus(float a);

    public abstract MutableVector2 minus(double a);

    public abstract MutableVector2 multiply(int a);

    public abstract MutableVector2 multiply(long a);

    public abstract MutableVector2 multiply(float a);

    public abstract MutableVector2 multiply(double a);

    public abstract MutableVector2 div(int a);

    public abstract MutableVector2 div(long a);

    public abstract MutableVector2 div(float a);

    public abstract MutableVector2 div(double a);

    public abstract MutableVector2 plus(Vector2 vector);

    public abstract MutableVector2 minus(Vector2 vector);

    public abstract MutableVector2 multiply(Vector2 vector);

    public abstract MutableVector2 div(Vector2 vector);

    public abstract MutableVector2 set(Vector2 a);

    public MutableVector2 set(int x, int y) {
        setX(x);
        setY(y);
        return this;
    }

    public MutableVector2 set(long x, long y) {
        setX(x);
        setY(y);
        return this;
    }

    public MutableVector2 set(float x, float y) {
        setX(x);
        setY(y);
        return this;
    }

    public MutableVector2 set(double x, double y) {
        setX(x);
        setY(y);
        return this;
    }

    public abstract MutableVector2 setX(int x);

    public abstract MutableVector2 setX(long x);

    public abstract MutableVector2 setX(float x);

    public abstract MutableVector2 setX(double x);

    public abstract MutableVector2 plusX(int x);

    public abstract MutableVector2 plusX(long x);

    public abstract MutableVector2 plusX(float x);

    public abstract MutableVector2 plusX(double x);

    public abstract MutableVector2 setY(int y);

    public abstract MutableVector2 setY(long y);

    public abstract MutableVector2 setY(float y);

    public abstract MutableVector2 setY(double y);

    public abstract MutableVector2 plusY(int y);

    public abstract MutableVector2 plusY(long y);

    public abstract MutableVector2 plusY(float y);

    public abstract MutableVector2 plusY(double y);

    public abstract int intX();

    public abstract long longX();

    public abstract float floatX();

    public abstract double doubleX();

    public byte byteX() {
        return (byte) intX();
    }

    public short shortX() {
        return (short) intX();
    }

    public abstract int intY();

    public abstract long longY();

    public abstract float floatY();

    public abstract double doubleY();

    public byte byteY() {
        return (byte) intY();
    }

    public short shortY() {
        return (short) intY();
    }

    public abstract Vector2 now();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
