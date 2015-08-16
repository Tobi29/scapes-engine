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

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Using {@linkplain #push()} you can retrieve objects from this pool, modify
 * them and then later iterate through them
 * <p>
 * As this class is meant for optimization of allocation and deallocation of
 * objects, any data in this {@code Pool} is kept until the {@code Pool} is
 * deleted by the GC
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
     * Creates a new {@code Pool} using the given {@code Supplier}
     *
     * @param supplier Called to create new objects in case the {@code Pool} ran out of
     *                 reusable ones
     */
    public Pool(Supplier<E> supplier) {
        this.supplier = supplier;
    }

    /**
     * Resets the {@code Pool} so it can be reused
     * <p>
     * <b>Note:</b> The stored objects are <b>not</b> cleared!
     */
    public void reset() {
        size = 0;
    }

    /**
     * Returns the next object from the {@code Pool} or creates a new ones if
     * none was available
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

    public E get(int i) {
        if (i >= size) {
            throw new IndexOutOfBoundsException(
                    "Index: " + i + " Size: " + size);
        }
        return list.get(i);
    }

    public E pop() {
        size--;
        return list.get(size);
    }

    public boolean remove(E element) {
        size--;
        return list.remove(element);
    }

    /**
     * Returns an {@code Iterator} to iterate through all objects previously
     * retrieved by {@linkplain #push()}
     *
     * @return An {@code Iterator} to iterate through the {@code Pool}'s data
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
        };
    }

    /**
     * Returns an {@code Spliterator} to iterate through all objects previously
     * retrieved by {@linkplain #push()}
     *
     * @return An {@code Spliterator} to iterate through the {@code Pool}'s data
     */
    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size, 0);
    }

    /**
     * Returns an {@code Stream} to iterate through all objects previously
     * retrieved by {@linkplain #push()}
     *
     * @return An {@code Stream} to iterate through the {@code Pool}'s data
     */
    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

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
