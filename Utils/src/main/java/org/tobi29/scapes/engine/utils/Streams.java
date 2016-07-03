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

/**
 * Utility class for converting various collections into a {@link Stream}
 */
public final class Streams {
    private Streams() {
    }

    /**
     * Converts given {@link Collection} into a {@link Stream}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #ofCollection(Collection)}
     *
     * @param collection The {@link Collection}
     * @param <T>        Element type
     * @return {@link Stream} providing the elements
     */
    public static <T> Stream<T> of(Collection<T> collection) {
        return ofCollection(collection);
    }

    /**
     * Converts given {@link Pool} into a {@link Stream}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link #ofPool(Pool)}
     *
     * @param pool The {@link Pool}
     * @param <T>  Element type
     * @return {@link Stream} providing the elements
     */
    public static <T> Stream<T> of(Pool<T> pool) {
        return ofPool(pool);
    }

    /**
     * Returns an empty {@link Stream}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link #ofEmpty()}
     *
     * @param <T> Element type
     * @return An empty {@link Stream}
     */
    public static <T> Stream<T> of() {
        return ofEmpty();
    }

    /**
     * Returns a {@link Stream} providing just the given item
     * <p>
     * <b>Note:</b> This is a more concise version of {@link #ofObject(Object)}
     *
     * @param item The only element to be in the {@link Stream}
     * @param <T>  Element type
     * @return A {@link Stream} with one element
     */
    public static <T> Stream<T> of(T item) {
        return ofObject(item);
    }

    /**
     * Returns a {@link Stream} providing just the given item if present
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #ofOptional(Optional)}
     *
     * @param item The only element to be in the {@link Stream} if present
     * @param <T>  Element type
     * @return A {@link Stream} with up to one element
     */
    public static <T> Stream<T> of(Optional<T> item) {
        return ofOptional(item);
    }

    /**
     * Converts given array into a {@link Stream}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #ofArray(Object[])}
     *
     * @param array The array
     * @param <T>   Element type
     * @return {@link Stream} providing the elements
     */
    @SafeVarargs
    public static <T> Stream<T> of(T... array) {
        return ofArray(array);
    }

    /**
     * Converts given {@link Spliterator} into a {@link Stream}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #ofSpliterator(Spliterator)}
     *
     * @param spliterator The {@link Spliterator}
     * @param <T>         Element type
     * @return {@link Stream} providing the elements
     */
    public static <T> Stream<T> of(Spliterator<T> spliterator) {
        return ofSpliterator(spliterator);
    }

    /**
     * Iterates through the given {@link Iterable} and passes the elements to
     * the {@link Consumer}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #forEachIterable(Iterable, Consumer)}
     *
     * @param iterable The {@link Iterable} to construct a for-each loop from
     * @param consumer {@link Consumer} that the elements are passed to
     * @param <T>      Element type
     */
    public static <T> void forEach(Iterable<T> iterable, Consumer<T> consumer) {
        forEachIterable(iterable, consumer);
    }

    /**
     * Iterates through the given array and passes the elements to the {@link
     * Consumer}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #forEachArray(Object[], Consumer)}
     *
     * @param array    The array to construct a for-each loop from
     * @param consumer {@link Consumer} that the elements are passed to
     * @param <T>      Element type
     */
    public static <T> void forEach(T[] array, Consumer<T> consumer) {
        forEachArray(array, consumer);
    }

    /**
     * Iterates through the given {@link Iterable}, filters out elements using
     * the {@link Predicate} and passes the remaining elements to the {@link
     * Consumer}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #forEachIterable(Iterable, Predicate, Consumer)}
     *
     * @param iterable The {@link Iterable} to construct a for-each loop from
     * @param filter   The {@link Predicate} to filter elements with
     * @param consumer {@link Consumer} that the elements are passed to
     * @param <T>      Element type
     */
    public static <T> void forEach(Iterable<T> iterable, Predicate<T> filter,
            Consumer<T> consumer) {
        forEachIterable(iterable, filter, consumer);
    }

    /**
     * Iterates through the given array, filters out elements using the {@link
     * Predicate} and passes the remaining elements to the {@link Consumer}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #forEachArray(Object[], Predicate, Consumer)}
     *
     * @param array    The array to construct a for-each loop from
     * @param filter   The {@link Predicate} to filter elements with
     * @param consumer {@link Consumer} that the elements are passed to
     * @param <T>      Element type
     */
    public static <T> void forEach(T[] array, Predicate<T> filter,
            Consumer<T> consumer) {
        forEachArray(array, filter, consumer);
    }

    /**
     * Iterates through the given {@link Iterable} and inserts them into an
     * {@link ArrayList}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #collectIterable(Iterable)}
     *
     * @param iterable The {@link Iterable} to construct a for-each loop from
     * @param <T>      Element type
     * @return An {@link ArrayList} containing the elements of the collection
     */
    public static <T> List<T> collect(Iterable<T> iterable) {
        return collectIterable(iterable);
    }

    /**
     * Inserts the item into an {@link ArrayList}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #collectObject(Object)}
     *
     * @param item The only element to be in the {@link ArrayList}
     * @param <T>  Element type
     * @return An {@link ArrayList} containing the element
     */
    public static <T> List<T> collect(T item) {
        return collectObject(item);
    }

    /**
     * Inserts the array into an {@link ArrayList}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #collectArray(Object[])}
     *
     * @param array The array to construct a for-each loop from
     * @param <T>   Element type
     * @return An {@link ArrayList} containing the elements of the array
     */
    @SafeVarargs
    public static <T> List<T> collect(T... array) {
        return collectArray(array);
    }

    /**
     * Iterates through the given {@link Iterable}, filters out elements using
     * the {@link Predicate} and inserts them into an {@link ArrayList}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #collectArray(Object[], Predicate)}
     *
     * @param iterable The {@link Iterable} to construct a for-each loop from
     * @param filter   The {@link Predicate} to filter elements with
     * @param <T>      Element type
     * @return An {@link ArrayList} containing the filtered elements of the
     * collection
     */
    public static <T> List<T> collect(Iterable<T> iterable,
            Predicate<T> filter) {
        return collectIterable(iterable, filter);
    }

    /**
     * Iterates through the given array, filters out elements using the {@link
     * Predicate} and inserts them into an {@link ArrayList}
     * <p>
     * <b>Note:</b> This is a more concise version of {@link
     * #collectArray(Object[], Predicate)}
     *
     * @param array  The array to construct a for-each loop from
     * @param filter The {@link Predicate} to filter elements with
     * @param <T>    Element type
     * @return An {@link ArrayList} containing the filtered elements of the
     * array
     */
    public static <T> List<T> collect(T[] array, Predicate<T> filter) {
        return collectArray(array, filter);
    }

    /**
     * Converts given {@link Collection} into a {@link Stream}
     *
     * @param collection The {@link Collection}
     * @param <T>        Element type
     * @return {@link Stream} providing the elements
     */
    public static <T> Stream<T> ofCollection(Collection<T> collection) {
        return StreamSupport.stream(collection);
    }

    /**
     * Converts given {@link Pool} into a {@link Stream}
     *
     * @param pool The {@link Pool}
     * @param <T>  Element type
     * @return {@link Stream} providing the elements
     */
    public static <T> Stream<T> ofPool(Pool<T> pool) {
        return of(pool.spliterator8());
    }

    /**
     * Returns an empty {@link Stream}
     *
     * @param <T> Element type
     * @return An empty {@link Stream}
     */
    public static <T> Stream<T> ofEmpty() {
        return RefStreams.empty();
    }

    /**
     * Returns a {@link Stream} providing just the given item
     *
     * @param item The only element to be in the {@link Stream}
     * @param <T>  Element type
     * @return A {@link Stream} with one element
     */
    public static <T> Stream<T> ofObject(T item) {
        return RefStreams.of(item);
    }

    /**
     * Returns a {@link Stream} providing just the given item if present
     *
     * @param item The only element to be in the {@link Stream} if present
     * @param <T>  Element type
     * @return A {@link Stream} with up to one element
     */
    public static <T> Stream<T> ofOptional(Optional<T> item) {
        if (item.isPresent()) {
            return RefStreams.of(item.get());
        }
        return of();
    }

    /**
     * Converts given array into a {@link Stream}
     *
     * @param array The array
     * @param <T>   Element type
     * @return {@link Stream} providing the elements
     */
    @SafeVarargs
    public static <T> Stream<T> ofArray(T... array) {
        return RefStreams.of(array);
    }

    /**
     * Converts given {@link Spliterator} into a {@link Stream}
     *
     * @param spliterator The {@link Spliterator}
     * @param <T>         Element type
     * @return {@link Stream} providing the elements
     */
    public static <T> Stream<T> ofSpliterator(Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Iterates through the given {@link Iterable} and passes the elements to
     * the {@link Consumer}
     *
     * @param iterable The {@link Iterable} to construct a for-each loop from
     * @param consumer {@link Consumer} that the elements are passed to
     * @param <T>      Element type
     */
    public static <T> void forEachIterable(Iterable<T> iterable,
            Consumer<T> consumer) {
        for (T entry : iterable) {
            consumer.accept(entry);
        }
    }

    /**
     * Iterates through the given array and passes the elements to the {@link
     * Consumer}
     *
     * @param array    The array to construct a for-each loop from
     * @param consumer {@link Consumer} that the elements are passed to
     * @param <T>      Element type
     */
    public static <T> void forEachArray(T[] array, Consumer<T> consumer) {
        for (T entry : array) {
            consumer.accept(entry);
        }
    }

    /**
     * Iterates through the given {@link Iterable}, filters out elements using
     * the {@link Predicate} and passes the remaining elements to the {@link
     * Consumer}
     *
     * @param iterable The {@link Iterable} to construct a for-each loop from
     * @param filter   The {@link Predicate} to filter elements with
     * @param consumer {@link Consumer} that the elements are passed to
     * @param <T>      Element type
     */
    public static <T> void forEachIterable(Iterable<T> iterable,
            Predicate<T> filter, Consumer<T> consumer) {
        for (T entry : iterable) {
            if (filter.test(entry)) {
                consumer.accept(entry);
            }
        }
    }

    /**
     * Iterates through the given array, filters out elements using the {@link
     * Predicate} and passes the remaining elements to the {@link Consumer}
     *
     * @param array    The array to construct a for-each loop from
     * @param filter   The {@link Predicate} to filter elements with
     * @param consumer {@link Consumer} that the elements are passed to
     * @param <T>      Element type
     */
    public static <T> void forEachArray(T[] array, Predicate<T> filter,
            Consumer<T> consumer) {
        for (T entry : array) {
            if (filter.test(entry)) {
                consumer.accept(entry);
            }
        }
    }

    /**
     * Iterates through the given {@link Iterable} and inserts them into an
     * {@link ArrayList}
     *
     * @param iterable The {@link Iterable} to construct a for-each loop from
     * @param <T>      Element type
     * @return An {@link ArrayList} containing the elements of the collection
     */
    public static <T> List<T> collectIterable(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for (T entry : iterable) {
            list.add(entry);
        }
        return list;
    }

    /**
     * Inserts the item into an {@link ArrayList}
     *
     * @param item The only element to be in the {@link ArrayList}
     * @param <T>  Element type
     * @return An {@link ArrayList} containing the element
     */
    public static <T> List<T> collectObject(T item) {
        List<T> list = new ArrayList<>(1);
        list.add(item);
        return list;
    }

    /**
     * Inserts the array into an {@link ArrayList}
     *
     * @param array The array to construct a for-each loop from
     * @param <T>   Element type
     * @return An {@link ArrayList} containing the elements of the array
     */
    @SafeVarargs
    public static <T> List<T> collectArray(T... array) {
        List<T> list = new ArrayList<>(array.length);
        Collections.addAll(list, array);
        return list;
    }

    /**
     * Iterates through the given {@link Iterable}, filters out elements using
     * the {@link Predicate} and inserts them into an {@link ArrayList}
     *
     * @param iterable The {@link Iterable} to construct a for-each loop from
     * @param filter   The {@link Predicate} to filter elements with
     * @param <T>      Element type
     * @return An {@link ArrayList} containing the filtered elements of the
     * collection
     */
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

    /**
     * Inserts the item into an {@link ArrayList} if the filter succeeds
     *
     * @param item   The only element to be in the {@link ArrayList}
     * @param filter The {@link Predicate} to filter the element element with
     * @param <T>    Element type
     * @return An {@link ArrayList} maybe containing the element
     */
    // There is no overloaded version of this as it conflicts with the probably
    // more useful iterable version
    public static <T> List<T> collectObject(T item, Predicate<T> filter) {
        if (!filter.test(item)) {
            return new ArrayList<>(0);
        }
        List<T> list = new ArrayList<>(1);
        list.add(item);
        return list;
    }

    /**
     * Iterates through the given array, filters out elements using the {@link
     * Predicate} and inserts them into an {@link ArrayList}
     *
     * @param array  The array to construct a for-each loop from
     * @param filter The {@link Predicate} to filter elements with
     * @param <T>    Element type
     * @return An {@link ArrayList} containing the filtered elements of the
     * array
     */
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
