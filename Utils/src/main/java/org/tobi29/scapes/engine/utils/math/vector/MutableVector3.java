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

public abstract class MutableVector3 extends MutableVector2 {
    @Override
    public abstract MutableVector3 plus(int a);

    @Override
    public abstract MutableVector3 plus(long a);

    @Override
    public abstract MutableVector3 plus(float a);

    @Override
    public abstract MutableVector3 plus(double a);

    @Override
    public abstract MutableVector3 minus(int a);

    @Override
    public abstract MutableVector3 minus(long a);

    @Override
    public abstract MutableVector3 minus(float a);

    @Override
    public abstract MutableVector3 minus(double a);

    @Override
    public abstract MutableVector3 multiply(int a);

    @Override
    public abstract MutableVector3 multiply(long a);

    @Override
    public abstract MutableVector3 multiply(float a);

    @Override
    public abstract MutableVector3 multiply(double a);

    @Override
    public abstract MutableVector3 div(int a);

    @Override
    public abstract MutableVector3 div(long a);

    @Override
    public abstract MutableVector3 div(float a);

    @Override
    public abstract MutableVector3 div(double a);

    @Override
    public abstract MutableVector3 plus(Vector2 vector);

    @Override
    public abstract MutableVector3 minus(Vector2 vector);

    @Override
    public abstract MutableVector3 multiply(Vector2 vector);

    @Override
    public abstract MutableVector3 div(Vector2 vector);

    @Override
    public abstract MutableVector3 set(Vector2 a);

    @Override
    public MutableVector3 set(int x, int y) {
        setX(x);
        setY(y);
        return this;
    }

    @Override
    public MutableVector3 set(long x, long y) {
        setX(x);
        setY(y);
        return this;
    }

    @Override
    public MutableVector3 set(float x, float y) {
        setX(x);
        setY(y);
        return this;
    }

    @Override
    public MutableVector3 set(double x, double y) {
        setX(x);
        setY(y);
        return this;
    }

    @Override
    public abstract MutableVector3 setX(int x);

    @Override
    public abstract MutableVector3 setX(long x);

    @Override
    public abstract MutableVector3 setX(float x);

    @Override
    public abstract MutableVector3 setX(double x);

    @Override
    public abstract MutableVector3 plusX(int x);

    @Override
    public abstract MutableVector3 plusX(long x);

    @Override
    public abstract MutableVector3 plusX(float x);

    @Override
    public abstract MutableVector3 plusX(double x);

    @Override
    public abstract MutableVector3 setY(int y);

    @Override
    public abstract MutableVector3 setY(long y);

    @Override
    public abstract MutableVector3 setY(float y);

    @Override
    public abstract MutableVector3 setY(double y);

    @Override
    public abstract MutableVector3 plusY(int y);

    @Override
    public abstract MutableVector3 plusY(long y);

    @Override
    public abstract MutableVector3 plusY(float y);

    @Override
    public abstract MutableVector3 plusY(double y);

    @Override
    public abstract int intX();

    @Override
    public abstract long longX();

    @Override
    public abstract float floatX();

    @Override
    public abstract double doubleX();

    @Override
    public byte byteX() {
        return (byte) intX();
    }

    @Override
    public short shortX() {
        return (short) intX();
    }

    @Override
    public abstract int intY();

    @Override
    public abstract long longY();

    @Override
    public abstract float floatY();

    @Override
    public abstract double doubleY();

    @Override
    public byte byteY() {
        return (byte) intY();
    }

    @Override
    public short shortY() {
        return (short) intY();
    }

    @Override
    public abstract Vector3 now();

    public abstract MutableVector3 plus(Vector3 vector);

    public abstract MutableVector3 minus(Vector3 vector);

    public abstract MutableVector3 multiply(Vector3 vector);

    public abstract MutableVector3 div(Vector3 vector);

    public abstract MutableVector3 set(Vector3 a);

    public MutableVector3 set(int x, int y, int z) {
        setX(x);
        setY(y);
        setZ(z);
        return this;
    }

    public MutableVector3 set(long x, long y, long z) {
        setX(x);
        setY(y);
        setZ(z);
        return this;
    }

    public MutableVector3 set(float x, float y, float z) {
        setX(x);
        setY(y);
        setZ(z);
        return this;
    }

    public MutableVector3 set(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
        return this;
    }

    public abstract MutableVector3 setZ(int z);

    public abstract MutableVector3 setZ(long z);

    public abstract MutableVector3 setZ(float z);

    public abstract MutableVector3 setZ(double z);

    public abstract MutableVector3 plusZ(int z);

    public abstract MutableVector3 plusZ(long z);

    public abstract MutableVector3 plusZ(float z);

    public abstract MutableVector3 plusZ(double z);

    public abstract int intZ();

    public abstract long longZ();

    public abstract float floatZ();

    public abstract double doubleZ();

    public byte byteZ() {
        return (byte) intZ();
    }
}
