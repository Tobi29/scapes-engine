package org.tobi29.scapes.engine.utils;

import java8.util.Optional;
import java8.util.Spliterator;
import java8.util.function.Consumer;
import java8.util.function.Predicate;
import java8.util.stream.RefStreams;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class Streams {
    private Streams() {
    }

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

    public static <T> void forEach(Iterable<T> iterable, Consumer<T> consumer) {
        forEachIterable(iterable, consumer);
    }

    public static <T> void forEach(T[] array, Consumer<T> consumer) {
        forEachArray(array, consumer);
    }

    public static <T> void forEach(Iterable<T> iterable, Predicate<T> filter,
            Consumer<T> consumer) {
        forEachIterable(iterable, filter, consumer);
    }

    public static <T> void forEach(T[] array, Predicate<T> filter,
            Consumer<T> consumer) {
        forEachArray(array, filter, consumer);
    }

    public static <T> List<T> collect(Iterable<T> iterable) {
        return collectIterable(iterable);
    }

    public static <T> List<T> collect(T[] array) {
        return collectArray(array);
    }

    public static <T> List<T> collect(Iterable<T> iterable,
            Predicate<T> filter) {
        return collectIterable(iterable, filter);
    }

    public static <T> List<T> collect(T[] array, Predicate<T> filter) {
        return collectArray(array, filter);
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

    public static <T> void forEachIterable(Iterable<T> iterable,
            Consumer<T> consumer) {
        for (T entry : iterable) {
            consumer.accept(entry);
        }
    }

    public static <T> void forEachArray(T[] array, Consumer<T> consumer) {
        for (T entry : array) {
            consumer.accept(entry);
        }
    }

    public static <T> void forEachIterable(Iterable<T> iterable,
            Predicate<T> filter, Consumer<T> consumer) {
        for (T entry : iterable) {
            if (filter.test(entry)) {
                consumer.accept(entry);
            }
        }
    }

    public static <T> void forEachArray(T[] array, Predicate<T> filter,
            Consumer<T> consumer) {
        for (T entry : array) {
            if (filter.test(entry)) {
                consumer.accept(entry);
            }
        }
    }

    public static <T> List<T> collectIterable(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for (T entry : iterable) {
            list.add(entry);
        }
        return list;
    }

    public static <T> List<T> collectArray(T[] array) {
        List<T> list = new ArrayList<>(array.length);
        Collections.addAll(list, array);
        return list;
    }

    public static <T> List<T> collectIterable(Iterable<T> iterable,
            Predicate<T> filter) {
        List<T> list = new ArrayList<>();
        for (T entry : iterable) {
            if (filter.test(entry)) {
                list.add(entry);
            }
        }
        return list;
    }

    public static <T> List<T> collectArray(T[] array, Predicate<T> filter) {
        List<T> list = new ArrayList<>(array.length);
        for (T entry : array) {
            if (filter.test(entry)) {
                list.add(entry);
            }
        }
        return list;
    }
}
