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

package kotlinx.coroutines.experimental.selects

import kotlinx.coroutines.experimental.DisposableHandle
import kotlinx.coroutines.experimental.MODE_DIRECT
import kotlinx.coroutines.experimental.internal.AtomicDesc
import kotlin.coroutines.experimental.Continuation

/**
 * Internal representation of select instance. This instance is called _selected_ when
 * the clause to execute is already picked.
 *
 * @suppress **This is unstable API and it is subject to change.**
 */
public interface SelectInstance<in R> {
    /**
     * Returns `true` when this [select] statement had already picked a clause to execute.
     */
    public val isSelected: Boolean

    /**
     * Tries to select this instance.
     */
    public fun trySelect(idempotent: Any?): Boolean

    /**
     * Performs action atomically with [trySelect].
     */
    public fun performAtomicTrySelect(desc: AtomicDesc): Any?

    /**
     * Performs action atomically when [isSelected] is `false`.
     */
    public fun performAtomicIfNotSelected(desc: AtomicDesc): Any?

    /**
     * Returns completion continuation of this select instance.
     * This select instance must be _selected_ first.
     * All resumption through this instance happen _directly_ (as if `mode` is [MODE_DIRECT]).
     */
    public val completion: Continuation<R>

    public fun resumeSelectWithException(exception: Throwable, mode: Int)

    // This function is actually implemented to dispose the handle only when the whole
    // select expression complete. It is later than it could be, but if resource will get released anyway
    // :todo: Invoke this function on select really
    public fun disposeOnSelect(handle: DisposableHandle)
}
