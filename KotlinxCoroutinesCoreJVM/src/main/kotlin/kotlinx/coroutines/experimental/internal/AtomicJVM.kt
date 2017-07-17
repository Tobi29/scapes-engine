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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

impl public abstract class AtomicOp : OpDescriptor() {
    @Volatile
    private var _consensus: Any? = UNDECIDED

    companion object {
        private val CONSENSUS: AtomicReferenceFieldUpdater<AtomicOp, Any?> =
                AtomicReferenceFieldUpdater.newUpdater(AtomicOp::class.java,
                        Any::class.java, "_consensus")

        private val UNDECIDED: Any = Symbol("UNDECIDED")
    }

    impl val isDecided: Boolean get() = _consensus !== UNDECIDED

    impl fun tryDecide(decision: Any?): Boolean {
        check(decision !== UNDECIDED)
        return CONSENSUS.compareAndSet(this, UNDECIDED, decision)
    }

    private fun decide(decision: Any?): Any? = if (tryDecide(
            decision)) decision else _consensus

    impl abstract fun prepare(): Any? // `null` if Ok, or failure reason

    impl abstract fun complete(affected: Any?,
                               failure: Any?) // failure != null if failed to prepare op

    // returns `null` on success
    impl final override fun perform(affected: Any?): Any? {
        // make decision on status
        var decision = this._consensus
        if (decision === UNDECIDED)
            decision = decide(prepare())
        complete(affected, decision)
        return decision
    }
}
