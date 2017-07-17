/*
 * Copyright 2016-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlinx.coroutines.experimental.internal

private typealias Node = LockFreeLinkedListNode

@PublishedApi
internal const val UNDECIDED = 0

@PublishedApi
internal const val SUCCESS = 1

@PublishedApi
internal const val FAILURE = 2

@PublishedApi
internal val CONDITION_FALSE: Any = Symbol("CONDITION_FALSE")

@PublishedApi
internal val ALREADY_REMOVED: Any = Symbol("ALREADY_REMOVED")

@PublishedApi
internal val LIST_EMPTY: Any = Symbol("LIST_EMPTY")

private val REMOVE_PREPARED: Any = Symbol("REMOVE_PREPARED")

/**
 * @suppress **This is unstable API and it is subject to change.**
 */
public typealias RemoveFirstDesc<T> = LockFreeLinkedListNode.RemoveFirstDesc<T>

/**
 * @suppress **This is unstable API and it is subject to change.**
 */
public typealias AddLastDesc<T> = LockFreeLinkedListNode.AddLastDesc<T>

/**
 * Doubly-linked concurrent list node with remove support.
 * Based on paper
 * ["Lock-Free and Practical Doubly Linked List-Based Deques Using Single-Word Compare-and-Swap"](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.140.4693&rep=rep1&type=pdf)
 * by Sundell and Tsigas.
 *
 * Important notes:
 * * The instance of this class serves both as list head/tail sentinel and as the list item.
 *   Sentinel node should be never removed.
 * * There are no operations to add items to left side of the list, only to the end (right side), because we cannot
 *   efficiently linearize them with atomic multi-step head-removal operations. In short,
 *   support for [describeRemoveFirst] operation precludes ability to add items at the beginning.
 *
 * @suppress **This is unstable API and it is subject to change.**
 */
@Suppress("LeakingThis")
header public open class LockFreeLinkedListNode {
    @PublishedApi
    header internal abstract class CondAddOp(newNode: Node) : AtomicOp() {
        val newNode: Node
        var oldNext: Node?

        override fun complete(affected: Any?,
                              failure: Any?)
    }

    @PublishedApi
    internal inline fun makeCondAddOp(node: Node,
                                      crossinline condition: () -> Boolean): CondAddOp

    public val isRemoved: Boolean

    // LINEARIZABLE. Returns Node | Removed
    public val next: Any

    // LINEARIZABLE. Returns Node | Removed
    public val prev: Any

    // ------ addOneIfEmpty ------

    public fun addOneIfEmpty(node: Node): Boolean

    // ------ addLastXXX ------

    /**
     * Adds last item to this list.
     */
    public fun addLast(node: Node)

    public fun <T : Node> describeAddLast(node: T): AddLastDesc<T>

    /**
     * Adds last item to this list atomically if the [condition] is true.
     */
    public inline fun addLastIf(node: Node,
                                crossinline condition: () -> Boolean): Boolean

    public inline fun addLastIfPrev(node: Node,
                                    predicate: (Node) -> Boolean): Boolean

    public inline fun addLastIfPrevAndIf(
            node: Node,
            predicate: (Node) -> Boolean, // prev node predicate
            crossinline condition: () -> Boolean // atomically checked condition
    ): Boolean

    // ------ addXXX util ------

    @PublishedApi
    internal fun addNext(node: Node,
                         next: Node): Boolean

    // returns UNDECIDED, SUCCESS or FAILURE
    @PublishedApi
    internal fun tryCondAddNext(node: Node,
                                next: Node,
                                condAdd: CondAddOp): Int

    // ------ removeXXX ------

    /**
     * Removes this node from the list. Returns `true` when removed successfully.
     */
    public open fun remove(): Boolean

    public open fun describeRemove(): AtomicDesc?

    public fun removeFirstOrNull(): Node?

    public fun describeRemoveFirst(): RemoveFirstDesc<Node>

    public inline fun <reified T> removeFirstIfIsInstanceOf(): T?

    // just peek at item when predicate is true
    public inline fun <reified T> removeFirstIfIsInstanceOfOrPeekIf(predicate: (T) -> Boolean): T?

    // ------ multi-word atomic operations helpers ------

    header public open class AddLastDesc<out T : Node>(
            queue: Node,
            node: T
    ) : AbstractAtomicDesc() {
        val queue: Node
        val node: T

        final override fun takeAffectedNode(op: OpDescriptor): Node

        final override var affectedNode: Node?
        final override val originalNext: Node?

        override fun retry(affected: Node,
                           next: Any): Boolean

        override fun onPrepare(affected: Node,
                               next: Node): Any?

        override fun updatedNext(affected: Node,
                                 next: Node): Any

        override fun finishOnSuccess(affected: Node,
                                     next: Node)
    }

    header public open class RemoveFirstDesc<T>(
            queue: Node
    ) : AbstractAtomicDesc() {
        val queue: Node
        public val result: T

        final override fun takeAffectedNode(op: OpDescriptor): Node
        final override var affectedNode: Node?
        final override var originalNext: Node?

        // check node predicates here, must signal failure if affect is not of type T
        protected override fun failure(affected: Node,
                                       next: Any): Any?

        // validate the resulting node (return false if it should be deleted)
        protected open fun validatePrepared(node: T): Boolean

        final override fun retry(affected: Node,
                                 next: Any): Boolean

        @Suppress("UNCHECKED_CAST")
        final override fun onPrepare(affected: Node,
                                     next: Node): Any?

        final override fun updatedNext(affected: Node,
                                       next: Node): Any

        final override fun finishOnSuccess(affected: Node,
                                           next: Node)
    }

    header public abstract class AbstractAtomicDesc : AtomicDesc() {
        protected abstract val affectedNode: Node?
        protected abstract val originalNext: Node?
        protected open fun takeAffectedNode(op: OpDescriptor): Node
        protected open fun failure(affected: Node,
                                   next: Any): Any?

        protected open fun retry(affected: Node,
                                 next: Any): Boolean

        protected abstract fun onPrepare(affected: Node,
                                         next: Node): Any? // non-null on failure

        protected abstract fun updatedNext(affected: Node,
                                           next: Node): Any

        protected abstract fun finishOnSuccess(affected: Node,
                                               next: Node)

        final override fun prepare(op: AtomicOp): Any?

        final override fun complete(op: AtomicOp,
                                    failure: Any?)
    }

    // ------ other helpers ------

    // fixes next links to the left of this node
    @PublishedApi
    internal fun helpDelete()

    internal fun validateNode(prev: Node,
                              next: Node)
}

internal class Removed(val ref: Node) {
    override fun toString(): String = "Removed[$ref]"
}

@PublishedApi
internal fun Any.unwrap(): Node = if (this is Removed) ref else this as Node

/**
 * Head (sentinel) item of the linked list that is never removed.
 *
 * @suppress **This is unstable API and it is subject to change.**
 */
public open class LockFreeLinkedListHead : LockFreeLinkedListNode() {
    public val isEmpty: Boolean get() = next === this

    /**
     * Iterates over all elements in this list of a specified type.
     */
    public inline fun <reified T : Node> forEach(block: (T) -> Unit) {
        var cur: Node = next as Node
        while (cur != this) {
            if (cur is T) block(cur)
            cur = cur.next.unwrap()
        }
    }

    // just a defensive programming -- makes sure that list head sentinel is never removed
    public final override fun remove() = throw UnsupportedOperationException()

    public final override fun describeRemove(): AtomicDesc? = throw UnsupportedOperationException()

    internal fun validate() {
        var prev: Node = this
        var cur: Node = next as Node
        while (cur != this) {
            val next = cur.next.unwrap()
            cur.validateNode(prev, next)
            prev = cur
            cur = next
        }
        validateNode(prev, next as Node)
    }
}
