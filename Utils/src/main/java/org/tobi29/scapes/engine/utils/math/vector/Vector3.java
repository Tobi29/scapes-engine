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

public abstract class Vector3 extends Vector2 {
    @Override
    public abstract Vector3 plus(int a);

    @Override
    public abstract Vector3 plus(long a);

    @Override
    public abstract Vector3 plus(float a);

    @Override
    public abstract Vector3 plus(double a);

    @Override
    public abstract Vector3 minus(int a);

    @Override
    public abstract Vector3 minus(long a);

    @Override
    public abstract Vector3 minus(float a);

    @Override
    public abstract Vector3 minus(double a);

    @Override
    public abstract Vector3 multiply(int a);

    @Override
    public abstract Vector3 multiply(long a);

    @Override
    public abstract Vector3 multiply(float a);

    @Override
    public abstract Vector3 multiply(double a);

    @Override
    public abstract Vector3 div(int a);

    @Override
    public abstract Vector3 div(long a);

    @Override
    public abstract Vector3 div(float a);

    @Override
    public abstract Vector3 div(double a);

    public abstract Vector3 plus(Vector3 vector);

    public abstract Vector3 minus(Vector3 vector);

    public abstract Vector3 multiply(Vector3 vector);

    public abstract Vector3 div(Vector3 vector);

    public abstract int intZ();

    public abstract long longZ();

    public abstract float floatZ();

    public abstract double doubleZ();

    public byte byteZ() {
        return (byte) intZ();
    }

    public short shortZ() {
        return (short) intZ();
    }
}
