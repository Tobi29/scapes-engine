package org.tobi29.scapes.engine.utils

impl infix fun (() -> Unit).chain(other: () -> Unit): () -> Unit =
        if (this is ChainedFunction0) {
            append(other)
        } else if (other is ChainedFunction0) {
            other.prepend(this)
        } else {
            ChainedFunction0(arrayOf(this, other))
        }

impl fun chain(vararg functions: () -> Unit): () -> Unit =
        ChainedFunction0(functions.flatMap {
            if (it is ChainedFunction0) {
                it.functions.asIterable()
            } else listOf(it)
        }.toTypedArray())

impl infix fun <P1> ((P1) -> Unit).chain(other: (P1) -> Unit): (P1) -> Unit =
        if (this is ChainedFunction1<*>) {
            (this as ChainedFunction1<P1>).append(other)
        } else if (other is ChainedFunction1<*>) {
            (other as ChainedFunction1<P1>).prepend(this)
        } else {
            ChainedFunction1(arrayOf(this, other))
        }

impl fun <P1> chain(vararg functions: (P1) -> Unit): (P1) -> Unit =
        ChainedFunction1(functions.flatMap {
            if (it is ChainedFunction1<*>) {
                (it as ChainedFunction1<P1>).functions.asIterable()
            } else listOf(it)
        }.toTypedArray())

impl infix fun <P1, P2> ((P1, P2) -> Unit).chain(other: (P1, P2) -> Unit): (P1, P2) -> Unit =
        if (this is ChainedFunction2<*, *>) {
            (this as ChainedFunction2<P1, P2>).append(other)
        } else if (other is ChainedFunction2<*, *>) {
            (other as ChainedFunction2<P1, P2>).prepend(this)
        } else {
            ChainedFunction2(arrayOf(this, other))
        }

impl fun <P1, P2> chain(vararg functions: (P1, P2) -> Unit): (P1, P2) -> Unit =
        ChainedFunction2(functions.flatMap {
            if (it is ChainedFunction2<*, *>) {
                (it as ChainedFunction2<P1, P2>).functions.asIterable()
            } else listOf(it)
        }.toTypedArray())

class ChainedFunction0(
        internal val functions: Array<() -> Unit>
) : Function0<Unit> {
    override operator fun invoke(): Unit {
        for (function in functions) {
            function()
        }
    }

    infix fun append(other: ChainedFunction0): ChainedFunction0 =
            append(other.functions)

    infix fun append(functions: Array<() -> Unit>): ChainedFunction0 =
            ChainedFunction0(this.functions + functions)

    infix fun append(function: () -> Unit): ChainedFunction0 =
            if (function is ChainedFunction0) {
                append(function)
            } else {
                ChainedFunction0(functions + function)
            }

    infix fun prepend(other: ChainedFunction0): ChainedFunction0 =
            prepend(other.functions)

    infix fun prepend(functions: Array<() -> Unit>): ChainedFunction0 =
            ChainedFunction0(functions + this.functions)

    infix fun prepend(function: () -> Unit): ChainedFunction0 =
            if (function is ChainedFunction0) {
                prepend(function)
            } else {
                ChainedFunction0(arrayOf(function, *functions))
            }
}

class ChainedFunction1<P1>(
        internal val functions: Array<(P1) -> Unit>
) : Function1<P1, Unit> {
    override operator fun invoke(p1: P1): Unit {
        for (function in functions) {
            function(p1)
        }
    }

    infix fun append(
            other: ChainedFunction1<in P1>
    ): ChainedFunction1<P1> =
            append(other.functions)

    infix fun append(
            functions: Array<out (P1) -> Unit>
    ): ChainedFunction1<P1> =
            ChainedFunction1(this.functions + functions)

    infix fun append(
            function: (P1) -> Unit
    ): ChainedFunction1<P1> =
            if (function is ChainedFunction1<*>) {
                append(function as ChainedFunction1<P1>)
            } else {
                ChainedFunction1(functions + function)
            }

    infix fun prepend(
            other: ChainedFunction1<P1>
    ): ChainedFunction1<P1> =
            prepend(other.functions)

    infix fun prepend(
            functions: Array<(P1) -> Unit>
    ): ChainedFunction1<P1> =
            ChainedFunction1(functions + this.functions)

    infix fun prepend(
            function: (P1) -> Unit
    ): ChainedFunction1<P1> =
            if (function is ChainedFunction1<*>) {
                prepend(function as ChainedFunction1<P1>)
            } else {
                ChainedFunction1(arrayOf(function, *functions))
            }
}


class ChainedFunction2<P1, P2>(
        internal val functions: Array<(P1, P2) -> Unit>
) : (P1, P2) -> Unit {
    override operator fun invoke(p1: P1,
                                 p2: P2): Unit {
        for (function in functions) {
            function(p1, p2)
        }
    }

    infix fun append(
            other: ChainedFunction2<in P1, in P2>
    ): ChainedFunction2<P1, P2> =
            append(other.functions)

    infix fun append(
            functions: Array<out (P1, P2) -> Unit>
    ): ChainedFunction2<P1, P2> =
            ChainedFunction2(this.functions + functions)

    infix fun append(
            function: (P1, P2) -> Unit
    ): ChainedFunction2<P1, P2> =
            if (function is ChainedFunction2<*, *>) {
                append(function as ChainedFunction2<P1, P2>)
            } else {
                ChainedFunction2(functions + function)
            }

    infix fun prepend(
            other: ChainedFunction2<P1, P2>
    ): ChainedFunction2<P1, P2> =
            prepend(other.functions)

    infix fun prepend(
            functions: Array<(P1, P2) -> Unit>
    ): ChainedFunction2<P1, P2> =
            ChainedFunction2(functions + this.functions)

    infix fun prepend(
            function: (P1, P2) -> Unit
    ): ChainedFunction2<P1, P2> =
            if (function is ChainedFunction2<*, *>) {
                prepend(function as ChainedFunction2<P1, P2>)
            } else {
                ChainedFunction2(arrayOf(function, *functions))
            }
}