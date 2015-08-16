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

public abstract class Vector2 extends Number {
    public abstract Vector2 plus(int a);

    public abstract Vector2 plus(long a);

    public abstract Vector2 plus(float a);

    public abstract Vector2 plus(double a);

    public abstract Vector2 minus(int a);

    public abstract Vector2 minus(long a);

    public abstract Vector2 minus(float a);

    public abstract Vector2 minus(double a);

    public abstract Vector2 multiply(int a);

    public abstract Vector2 multiply(long a);

    public abstract Vector2 multiply(float a);

    public abstract Vector2 multiply(double a);

    public abstract Vector2 div(int a);

    public abstract Vector2 div(long a);

    public abstract Vector2 div(float a);

    public abstract Vector2 div(double a);

    public abstract Vector2 plus(Vector2 vector);

    public abstract Vector2 minus(Vector2 vector);

    public abstract Vector2 multiply(Vector2 vector);

    public abstract Vector2 div(Vector2 vector);

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

    public abstract boolean hasNaN();

    @Override
    public int intValue() {
        return intX();
    }

    @Override
    public long longValue() {
        return longX();
    }

    @Override
    public float floatValue() {
        return floatX();
    }

    @Override
    public double doubleValue() {
        return doubleX();
    }

    @Override
    public byte byteValue() {
        return byteX();
    }

    @Override
    public short shortValue() {
        return shortX();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
