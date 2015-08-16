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

public class MutablePair<A, B> {
    public A a;
    public B b;

    public MutablePair() {
    }

    public MutablePair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public void set(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A a() {
        return a;
    }

    public B b() {
        return b;
    }

    @Override
    public int hashCode() {
        A a = this.a;
        B b = this.b;
        int prime = 31;
        int result = 1;
        result = prime * result + (a == null ? 0 : a.hashCode());
        result = prime * result + (b == null ? 0 : b.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        A a = this.a;
        B b = this.b;
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MutablePair)) {
            return false;
        }
        MutablePair<?, ?> other = (MutablePair<?, ?>) obj;
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
        return true;
    }

    @Override
    public String toString() {
        A a = this.a;
        B b = this.b;
        return (a == null ? null : a.toString()) + '/' +
                (b == null ? null : b.toString());
    }
}
