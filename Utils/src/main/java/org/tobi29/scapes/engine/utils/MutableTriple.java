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

package org.tobi29.scapes.engine.utils;

public class MutableTriple<A, B, C> {
    public A a;
    public B b;
    public C c;

    public MutableTriple() {
    }

    public MutableTriple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public void set(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A a() {
        return a;
    }

    public B b() {
        return b;
    }

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
