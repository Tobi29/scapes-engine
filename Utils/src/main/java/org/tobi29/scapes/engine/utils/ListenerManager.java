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

import java8.util.function.Consumer;
import java8.util.function.Predicate;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public class ListenerManager<L> {
    private final Map<ListenerOwnerHandle, WeakReference<L>> listeners =
            new WeakHashMap<>();

    public void add(ListenerOwner owner, L listener) {
        add(owner.owner(), listener);
    }

    public synchronized void add(ListenerOwnerHandle owner, L listener) {
        owner.add(listener);
        listeners.put(owner, new WeakReference<>(listener));
    }

    public synchronized void fire(Consumer<L> consumer) {
        for (Map.Entry<ListenerOwnerHandle, WeakReference<L>> entry : listeners
                .entrySet()) {
            if (!entry.getKey().isValid()) {
                continue;
            }
            L listener = entry.getValue().get();
            if (listener == null) {
                continue;
            }
            consumer.accept(listener);
        }
    }

    public synchronized boolean fireReturn(Predicate<L> consumer) {
        for (Map.Entry<ListenerOwnerHandle, WeakReference<L>> entry : listeners
                .entrySet()) {
            if (!entry.getKey().isValid()) {
                continue;
            }
            L listener = entry.getValue().get();
            if (listener == null) {
                continue;
            }
            if (consumer.test(listener)) {
                return true;
            }
        }
        return false;
    }
}
