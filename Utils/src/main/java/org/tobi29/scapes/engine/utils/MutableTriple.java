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
 * Class implementing a basic mutable reference to two objects
 *
 * @param <A> Type of the first reference
 * @param <B> Type of the second reference
 * @param <C> Type of the third reference
 */
public class MutableTriple<A, B, C> {
    /**
     * First object that this instance holds
     */
    public A a;
    /**
     * Second object that this instance holds
     */
    public B b;
    /**
     * Third object that this instance holds
     */
    public C c;

    /**
     * Construct a new instance with {@code null}
     */
    public MutableTriple() {
    }

    /**
     * Construct a new instance given objects
     *
     * @param a Object to assign to {@link #a}
     * @param b Object to assign to {@link #b}
     * @param c Object to assign to {@link #c}
     */
    public MutableTriple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Setter for changing object, useful for method references
     *
     * @param a Object to assign to {@link #a}
     * @param b Object to assign to {@link #b}
     * @param c Object to assign to {@link #c}
     */
    public void set(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * Setter for changing object, useful for method references
     *
     * @param a Object to assign to {@link #a}
     */
    public void setA(A a) {
        this.a = a;
    }

    /**
     * Setter for changing object, useful for method references
     *
     * @param b Object to assign to {@link #b}
     */
    public void setB(B b) {
        this.b = b;
    }

    /**
     * Setter for changing object, useful for method references
     *
     * @param c Object to assign to {@link #c}
     */
    public void setC(C c) {
        this.c = c;
    }

    /**
     * Returns an object of this instance
     *
     * @return {@link #a}
     */
    public A a() {
        return a;
    }

    /**
     * Returns an object of this instance
     *
     * @return {@link #b}
     */
    public B b() {
        return b;
    }

    /**
     * Returns an object of this instance
     *
     * @return {@link #c}
     */
    public C c() {
        return c;
    }

    @Override
    public int hashCode() {
        A a = this.a;
        B b = this.b;
        C c = this.c;
        int prime = 31;
        int result = 1;
        result = prime * result + (a == null ? 0 : a.hashCode());
        result = prime * result + (b == null ? 0 : b.hashCode());
        result = prime * result + (c == null ? 0 : c.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        A a = this.a;
        B b = this.b;
        C c = this.c;
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MutableTriple)) {
            return false;
        }
        MutableTriple<?, ?, ?> other = (MutableTriple<?, ?, ?>) obj;
        if (a == null) {
            if (other.a != null) {
                return false;
            }
        } else if (!a.equals(other.a)) {
            return false;
        }
        if (b == null) {
            if (other.b != null) {
                return false;
            }
        } else if (!b.equals(other.b)) {
            return false;
        }
        if (c == null) {
            if (other.c != null) {
                return false;
            }
        } else if (!c.equals(other.c)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        A a = this.a;
        B b = this.b;
        C c = this.c;
        return (a == null ? null : a.toString()) + '/' +
                (b == null ? null : b.toString()) + '/' +
                (c == null ? null : c.toString());
    }
}
