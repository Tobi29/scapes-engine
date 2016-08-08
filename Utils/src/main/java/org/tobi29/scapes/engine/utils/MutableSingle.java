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
 * Class implementing a basic mutable reference to an object
 *
 * @param <A> Type of the reference
 */
public class MutableSingle<A> {
    /**
     * Object that this instance holds
     */
    public A a;

    /**
     * Construct a new instance with {@code null}
     */
    public MutableSingle() {
    }

    /**
     * Construct a new instance given object
     *
     * @param a Object to assign to {@link #a}
     */
    public MutableSingle(A a) {
        this.a = a;
    }

    /**
     * Setter for changing object, useful for method references
     *
     * @param a Object to assign to {@link #a}
     */
    public void set(A a) {
        this.a = a;
    }

    /**
     * Returns object of this instance
     *
     * @return {@link #a}
     */
    public A a() {
        return a;
    }

    @Override
    public int hashCode() {
        A a = this.a;
        return a == null ? 0 : a.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        A a = this.a;
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MutableSingle)) {
            return false;
        }
        MutableSingle<?> other = (MutableSingle<?>) obj;
        if (a == null) {
            if (other.a != null) {
                return false;
            }
        } else if (!a.equals(other.a)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        A a = this.a;
        return a == null ? "null" : a.toString();
    }
}
