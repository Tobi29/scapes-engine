package org.tobi29.scapes.engine.utils

header class AtomicBoolean(value: Boolean) {
    fun get(): Boolean

    fun compareAndSet(expect: Boolean,
                      update: Boolean): Boolean

    fun set(newValue: Boolean)

    fun getAndSet(newValue: Boolean): Boolean
}

header class AtomicInteger(value: Int) {
    fun get(): Int

    fun set(newValue: Int)

    fun getAndSet(newValue: Int): Int

    fun compareAndSet(expect: Int,
                      update: Int): Boolean

    fun getAndIncrement(): Int

    fun getAndDecrement(): Int

    fun getAndAdd(delta: Int): Int

    fun incrementAndGet(): Int

    fun decrementAndGet(): Int

    fun addAndGet(delta: Int): Int
}

header class AtomicLong(value: Long) {
    fun get(): Long

    fun set(newValue: Long)

    fun getAndSet(newValue: Long): Long

    fun compareAndSet(expect: Long,
                      update: Long): Boolean

    fun getAndIncrement(): Long

    fun getAndDecrement(): Long

    fun getAndAdd(delta: Long): Long

    fun incrementAndGet(): Long

    fun decrementAndGet(): Long

    fun addAndGet(delta: Long): Long
}

header class AtomicReference<V>(value: V) {
    fun get(): V

    fun set(newValue: V)

    fun compareAndSet(expect: V,
                      update: V): Boolean

    fun getAndSet(newValue: V): V
}
