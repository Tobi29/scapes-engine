/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.assertions

import org.jetbrains.spek.api.dsl.ActionBody
import org.jetbrains.spek.api.dsl.SpecBody
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.data_driven.*

inline fun <I1, Expected> SpecBody.on(
    description: (I1) -> String,
    vararg with: Data1<I1, Expected>,
    crossinline body: ActionBody.(I1, Expected) -> Unit
) {
    with.forEach { (i1, expected) ->
        on(description(i1)) {
            body(i1, expected)
        }
    }
}

inline fun <I1, Expected> SpecBody.on(
    description: (I1, Expected) -> String,
    vararg with: Data1<I1, Expected>,
    crossinline body: ActionBody.(I1, Expected) -> Unit
) {
    with.forEach { (i1, expected) ->
        on(description(i1, expected)) {
            body(i1, expected)
        }
    }
}

inline fun <I1, I2, Expected> SpecBody.on(
    description: (I1, I2) -> String,
    vararg with: Data2<I1, I2, Expected>,
    crossinline body: ActionBody.(I1, I2, Expected) -> Unit
) {
    with.forEach { (i1, i2, expected) ->
        on(description(i1, i2)) {
            body(i1, i2, expected)
        }
    }
}

inline fun <I1, I2, Expected> SpecBody.on(
    description: (I1, I2, Expected) -> String,
    vararg with: Data2<I1, I2, Expected>,
    crossinline body: ActionBody.(I1, I2, Expected) -> Unit
) {
    with.forEach { (i1, i2, expected) ->
        on(description(i1, i2, expected)) {
            body(i1, i2, expected)
        }
    }
}

inline fun <I1, I2, I3, Expected> SpecBody.on(
    description: (I1, I2, I3) -> String,
    vararg with: Data3<I1, I2, I3, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, expected) ->
        on(description(i1, i2, i3)) {
            body(i1, i2, i3, expected)
        }
    }
}

inline fun <I1, I2, I3, Expected> SpecBody.on(
    description: (I1, I2, I3, Expected) -> String,
    vararg with: Data3<I1, I2, I3, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, expected) ->
        on(description(i1, i2, i3, expected)) {
            body(i1, i2, i3, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, Expected> SpecBody.on(
    description: (I1, I2, I3, I4) -> String,
    vararg with: Data4<I1, I2, I3, I4, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, expected) ->
        on(description(i1, i2, i3, i4)) {
            body(i1, i2, i3, i4, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, Expected) -> String,
    vararg with: Data4<I1, I2, I3, I4, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, expected) ->
        on(description(i1, i2, i3, i4, expected)) {
            body(i1, i2, i3, i4, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, I5) -> String,
    vararg with: Data5<I1, I2, I3, I4, I5, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, I5, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, expected) ->
        on(description(i1, i2, i3, i4, i5)) {
            body(i1, i2, i3, i4, i5, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, I5, Expected) -> String,
    vararg with: Data5<I1, I2, I3, I4, I5, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, I5, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, expected) ->
        on(description(i1, i2, i3, i4, i5, expected)) {
            body(i1, i2, i3, i4, i5, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, I5, I6) -> String,
    vararg with: Data6<I1, I2, I3, I4, I5, I6, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, I5, I6, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, expected) ->
        on(description(i1, i2, i3, i4, i5, i6)) {
            body(i1, i2, i3, i4, i5, i6, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, I5, I6, Expected) -> String,
    vararg with: Data6<I1, I2, I3, I4, I5, I6, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, I5, I6, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, expected) ->
        on(description(i1, i2, i3, i4, i5, i6, expected)) {
            body(i1, i2, i3, i4, i5, i6, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, I7, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, I5, I6, I7) -> String,
    vararg with: Data7<I1, I2, I3, I4, I5, I6, I7, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, I5, I6, I7, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, expected) ->
        on(description(i1, i2, i3, i4, i5, i6, i7)) {
            body(i1, i2, i3, i4, i5, i6, i7, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, I7, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, I5, I6, I7, Expected) -> String,
    vararg with: Data7<I1, I2, I3, I4, I5, I6, I7, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, I5, I6, I7, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, expected) ->
        on(description(i1, i2, i3, i4, i5, i6, i7, expected)) {
            body(i1, i2, i3, i4, i5, i6, i7, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, I7, I8, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, I5, I6, I7, I8) -> String,
    vararg with: Data8<I1, I2, I3, I4, I5, I6, I7, I8, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, I5, I6, I7, I8, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, i8, expected) ->
        on(description(i1, i2, i3, i4, i5, i6, i7, i8)) {
            body(i1, i2, i3, i4, i5, i6, i7, i8, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, I7, I8, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, I5, I6, I7, I8, Expected) -> String,
    vararg with: Data8<I1, I2, I3, I4, I5, I6, I7, I8, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, I5, I6, I7, I8, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, i8, expected) ->
        on(description(i1, i2, i3, i4, i5, i6, i7, i8, expected)) {
            body(i1, i2, i3, i4, i5, i6, i7, i8, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, I5, I6, I7, I8, I9) -> String,
    vararg with: Data9<I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, i8, i9, expected) ->
        on(description(i1, i2, i3, i4, i5, i6, i7, i8, i9)) {
            body(i1, i2, i3, i4, i5, i6, i7, i8, i9, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected> SpecBody.on(
    description: (I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected) -> String,
    vararg with: Data9<I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected>,
    crossinline body: ActionBody.(I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, i8, i9, expected) ->
        on(description(i1, i2, i3, i4, i5, i6, i7, i8, i9, expected)) {
            body(i1, i2, i3, i4, i5, i6, i7, i8, i9, expected)
        }
    }
}
