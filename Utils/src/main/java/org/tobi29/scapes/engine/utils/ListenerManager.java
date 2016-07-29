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
