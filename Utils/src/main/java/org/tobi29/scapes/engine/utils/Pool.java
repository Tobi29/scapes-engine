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

import java8.util.Spliterator;
import java8.util.Spliterators;
import java8.util.function.Supplier;
import java8.util.stream.Stream;
import java8.util.stream.StreamSupport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Using {@link #push()} you can retrieve objects from this pool, modify them
 * and then later iterate through them
 * <p>
 * As this class is meant for optimization of allocation and deallocation of
 * objects, any data in the pool is kept until the instance is deleted by the GC
 * or you removed it using {@link #remove(Object)}
 * <p>
 * <b>Note:</b> This class is not thread-safe at all and should only be used by
 * one thread at a time
 *
 * @param <E> Type of elements stored
 */
public class Pool<E> implements Iterable<E> {
    private final Supplier<E> supplier;
    private final List<E> list = new ArrayList<>();
    private int size;

    /**
     * Creates a new instance using the given {@link Supplier}
     *
     * @param supplier Called to create new objects in case the pool ran out of
     *                 reusable ones
     */
    public Pool(Supplier<E> supplier) {
        this.supplier = supplier;
    }

    /**
     * Resets the pool so it can be reused
     * <p>
     * <b>Note:</b> The stored objects are <b>not</b> cleared!
     */
    public void reset() {
        size = 0;
    }

    /**
     * Returns the next object from the pool or creates a new ones if none was
     * available
     *
     * @return A possibly reused object
     */
    public E push() {
        E value;
        if (list.size() <= size) {
            value = supplier.get();
            list.add(value);
            size++;
        } else {
            value = list.get(size++);
        }
        return value;
    }

    /**
     * Returns the object at the given index
     *
     * @param i Index to look at (Has to be in range {@code 0} to {@code size -
     *          1})
     * @return Object at given index
     * @throws IndexOutOfBoundsException When index is equal or greater than
     *                                   size or less than 0
     */
    public E get(int i) {
        if (i < 0 || i >= size) {
            throw new IndexOutOfBoundsException(
                    "Index: " + i + " Size: " + size);
        }
        return list.get(i);
    }

    /**
     * Returns the latest object in the pool returned from {@link #push()}
     *
     * @return Last object in pool
     */
    public E pop() {
        if (size == 0) {
            throw new NoSuchElementException("Pool is empty");
        }
        size--;
        return list.get(size);
    }

    /**
     * Removes the given object out of the pool
     * <p>
     * <b>Note:</b> The object it removed even after calling reset
     *
     * @param element Object to remove
     * @return When {@code true} the object is no longer referenced by the pool,
     * otherwise it never was to begin with
     */
    public boolean remove(E element) {
        if (list.remove(element)) {
            size--;
            return true;
        }
        return false;
    }

    /**
     * Returns an {@link Iterator} to iterate through all objects previously
     * retrieved by {@link #push()}
     *
     * @return An {@link Iterator} to iterate through the pool's data
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int i;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public E next() {
                if (i >= size) {
                    throw new NoSuchElementException(
                            "Reached limit: " + i + " of " + size);
                }
                return list.get(i++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Cannot remove object from pool");
            }
        };
    }

    /**
     * Returns an {@link Spliterator} to iterate through all objects previously
     * retrieved by {@link #push()}
     *
     * @return An {@link Spliterator} to iterate through the pool's data
     */
    public Spliterator<E> spliterator8() {
        return Spliterators.spliterator(iterator(), size, 0);
    }

    /**
     * Returns an {@link Stream} to iterate through all objects previously
     * retrieved by {@link #push()}
     *
     * @return An {@link Stream} to iterate through the pool's data
     */
    public Stream<E> stream() {
        return StreamSupport.stream(spliterator8(), false);
    }

    /**
     * Returns whether or not the given object can be found in the pool
     * <p>
     * <b>Note:</b> Object outside the range are not checked
     *
     * @param element The object to search or {@code null}
     * @return {@code true} if the object was found
     */
    public boolean contains(E element) {
        if (element == null) {
            for (int i = 0; i < size; i++) {
                if (list.get(i) == null) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (element.equals(list.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
