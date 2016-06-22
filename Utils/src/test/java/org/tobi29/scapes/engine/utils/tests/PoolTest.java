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
package org.tobi29.scapes.engine.utils.tests;

import org.junit.Assert;
import org.junit.Test;
import org.tobi29.scapes.engine.utils.MutableSingle;
import org.tobi29.scapes.engine.utils.Pool;
import org.tobi29.scapes.engine.utils.Streams;

import java.util.NoSuchElementException;

public class PoolTest {
    private static void fillPool(Pool<MutableSingle<String>> pool, int size) {
        for (int i = 0; i < size; i++) {
            pool.push();
        }
        pool.reset();
    }

    @Test
    public void testStream() {
        Pool<MutableSingle<String>> pool =
                new Pool<>(MutableSingle<String>::new);
        fillPool(pool, 4);
        pool.push().set("4");
        pool.push().set("5");
        pool.push().set("6");
        pool.push().set("1");
        pool.push().set("2");
        pool.push().set("3");
        StringBuilder output = new StringBuilder(6);
        Streams.of(pool).map(MutableSingle::a).forEach(output::append);
        Assert.assertEquals("Unexpected output string", output.toString(),
                "456123");
    }

    @Test
    public void testSortedStream() {
        Pool<MutableSingle<String>> pool =
                new Pool<>(MutableSingle<String>::new);
        fillPool(pool, 4);
        pool.push().set("4");
        pool.push().set("5");
        pool.push().set("6");
        pool.push().set("1");
        pool.push().set("2");
        pool.push().set("3");
        StringBuilder output = new StringBuilder(6);
        Streams.of(pool).map(MutableSingle::a).sorted().forEach(output::append);
        Assert.assertEquals("Unexpected output string", output.toString(),
                "123456");
    }

    @Test
    public void testPop() {
        Pool<MutableSingle<String>> pool =
                new Pool<>(MutableSingle<String>::new);
        fillPool(pool, 4);
        pool.push().set("1");
        pool.push().set("2");
        pool.push().set("3");
        Assert.assertEquals("Popping returned wrong object", pool.pop().a, "3");
        Assert.assertEquals("Popping returned wrong object", pool.pop().a, "2");
        pool.push().set("4");
        Assert.assertEquals("Popping returned wrong object", pool.pop().a, "4");
        Assert.assertEquals("Popping returned wrong object", pool.pop().a, "1");
        Assert.assertTrue("Pool is not empty", pool.isEmpty());
        try {
            pool.pop();
            Assert.fail("Pool did not throw when popping on empty");
        } catch (NoSuchElementException e) {
        }
    }

    @Test
    public void testRemove() {
        Pool<MutableSingle<String>> pool =
                new Pool<>(MutableSingle<String>::new);
        fillPool(pool, 4);
        pool.push().set("1");
        pool.push().set("2");
        pool.push().set("3");
        Assert.assertTrue("Object not in pool",
                pool.contains(new MutableSingle<>("2")));
        pool.remove(new MutableSingle<>("2"));
        Assert.assertFalse("Object in pool",
                pool.contains(new MutableSingle<>("2")));
    }
}
