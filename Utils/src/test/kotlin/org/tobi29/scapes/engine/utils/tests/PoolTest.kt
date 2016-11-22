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
package org.tobi29.scapes.engine.utils.tests

import org.junit.Assert
import org.junit.Test
import org.tobi29.scapes.engine.utils.Pool
import java.util.*

class PoolTest {
    private fun fillPool(pool: Pool<StringHolder>,
                         size: Int) {
        for (i in 0..size - 1) {
            pool.push()
        }
        pool.reset()
    }

    @Test
    fun testStream() {
        val pool = Pool({ StringHolder() })
        fillPool(pool, 4)
        pool.push().set("4")
        pool.push().set("5")
        pool.push().set("6")
        pool.push().set("1")
        pool.push().set("2")
        pool.push().set("3")
        val output = StringBuilder(6)
        pool.stream().map { it.str }.forEach { output.append(it) }
        Assert.assertEquals("Unexpected output string", output.toString(),
                "456123")
    }

    @Test
    fun testSortedStream() {
        val pool = Pool({ StringHolder() })
        fillPool(pool, 4)
        pool.push().set("4")
        pool.push().set("5")
        pool.push().set("6")
        pool.push().set("1")
        pool.push().set("2")
        pool.push().set("3")
        val output = StringBuilder(6)
        pool.stream().map { it.str }.sorted().forEach { output.append(it) }
        Assert.assertEquals("Unexpected output string", output.toString(),
                "123456")
    }

    @Test
    fun testPop() {
        val pool = Pool({ StringHolder() })
        fillPool(pool, 4)
        pool.push().set("1")
        pool.push().set("2")
        pool.push().set("3")
        Assert.assertEquals("Popping returned wrong object", "2",
                pool.pop()?.str)
        Assert.assertEquals("Popping returned wrong object", "1",
                pool.pop()?.str)
        pool.push().set("4")
        Assert.assertEquals("Popping returned wrong object", "1",
                pool.pop()?.str)
        Assert.assertEquals("Popping returned wrong object", null,
                pool.pop()?.str)
        Assert.assertTrue("Pool is not empty", pool.isEmpty())
        try {
            pool.pop()
            Assert.fail("Pool did not throw when popping on empty")
        } catch (e: NoSuchElementException) {
        }

    }

    @Test
    fun testRemove() {
        val pool = Pool({ StringHolder() })
        fillPool(pool, 4)
        pool.push().set("1")
        pool.push().set("2")
        pool.push().set("3")
        Assert.assertTrue("Object not in pool",
                pool.contains(StringHolder("2")))
        pool.remove(StringHolder("2"))
        Assert.assertFalse("Object in pool", pool.contains(StringHolder("2")))
    }

    data class StringHolder(var str: String = "") {
        fun set(str: String) {
            this.str = str
        }
    }
}
