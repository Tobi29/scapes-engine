package org.tobi29.scapes.engine.utils;

import java8.util.function.BooleanSupplier;

import java.util.HashSet;
import java.util.Set;

public class ListenerOwnerHandle {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Set<Object> listeners = new HashSet<>();
    private final BooleanSupplier valid;

    public ListenerOwnerHandle(BooleanSupplier valid) {
        this.valid = valid;
    }

    public void add(Object listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public boolean isValid() {
        return valid.getAsBoolean();
    }
}
