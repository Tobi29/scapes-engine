/*
 * Copyright 2012-2016 Tobi29
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
package org.tobi29.scapes.engine.utils;

/**
 * Class implementing a basic mutable reference to a primitive integer
 */
public class MutableInteger {
    /**
     * Value that this instance holds
     */
    public int a;

    /**
     * Construct a new instance with value {@code 0.0}
     */
    public MutableInteger() {
    }

    /**
     * Construct a new instance given value
     *
     * @param a Value to assign to {@link #a}
     */
    public MutableInteger(int a) {
        this.a = a;
    }

    /**
     * Setter for changing value, useful for method references
     *
     * @param a Value to assign to {@link #a}
     */
    public void set(int a) {
        this.a = a;
    }

    /**
     * Returns value of this instance
     *
     * @return {@link #a}
     */
    public int a() {
        return a;
    }

    @Override
    public int hashCode() {
        return a;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MutableInteger)) {
            return false;
        }
        MutableInteger other = (MutableInteger) obj;
        return a == other.a;
    }

    @Override
    public String toString() {
        return Integer.toString(a);
    }
}
