package org.tobi29.scapes.engine.utils;

import java8.util.Optional;
import java8.util.Spliterator;
import java8.util.stream.RefStreams;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

import java.util.Collection;

public class Streams {
    public static <T> Stream<T> of(Collection<T> collection) {
        return ofCollection(collection);
    }

    public static <T> Stream<T> of(Pool<T> pool) {
        return ofPool(pool);
    }

    public static <T> Stream<T> of() {
        return ofEmpty();
    }

    public static <T> Stream<T> of(T item) {
        return ofObject(item);
    }

    public static <T> Stream<T> of(Optional<T> item) {
        return ofOptional(item);
    }

    @SafeVarargs
    public static <T> Stream<T> of(T... array) {
        return ofArray(array);
    }

    public static <T> Stream<T> of(Spliterator<T> spliterator) {
        return ofSpliterator(spliterator);
    }

    public static <T> Stream<T> ofCollection(Collection<T> collection) {
        return StreamSupport.stream(collection);
    }

    public static <T> Stream<T> ofPool(Pool<T> pool) {
        return of(pool.spliterator8());
    }

    public static <T> Stream<T> ofEmpty() {
        return RefStreams.empty();
    }

    public static <T> Stream<T> ofObject(T item) {
        return RefStreams.of(item);
    }

    public static <T> Stream<T> ofOptional(Optional<T> item) {
        if (item.isPresent()) {
            return RefStreams.of(item.get());
        }
        return of();
    }

    @SafeVarargs
    public static <T> Stream<T> ofArray(T... array) {
        return RefStreams.of(array);
    }

    public static <T> Stream<T> ofSpliterator(Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }
}
