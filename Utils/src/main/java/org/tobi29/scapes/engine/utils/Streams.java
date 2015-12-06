package org.tobi29.scapes.engine.utils;

import java8.util.Spliterator;
import java8.util.stream.RefStreams;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

import java.util.Collection;

public class Streams {
    public static <T> Stream<T> of(Collection<T> collection) {
        return StreamSupport.stream(collection);
    }

    public static <T> Stream<T> of(Pool<T> pool) {
        return of(pool.spliterator8());
    }

    public static <T> Stream<T> of() {
        return RefStreams.empty();
    }

    public static <T> Stream<T> of(T item) {
        return RefStreams.of(item);
    }

    @SafeVarargs
    public static <T> Stream<T> of(T... array) {
        return RefStreams.of(array);
    }

    public static <T> Stream<T> of(Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }
}
