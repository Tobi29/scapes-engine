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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.assertions

import org.spekframework.spek2.style.specification.Suite

data class Data1<I1, Expected>(
    val i1: I1,
    val expected: Expected
)

inline fun <I1, Expected> data(
    i1: I1,
    expected: Expected
) = Data1(i1, expected)

inline fun <I1, Expected> Suite.data(
    description: (I1) -> String,
    vararg with: Data1<I1, Expected>,
    crossinline body: Suite.(I1, Expected) -> Unit
) {
    with.forEach { (i1, expected) ->
        describe(description(i1)) {
            body(i1, expected)
        }
    }
}

inline fun <I1, Expected> Suite.data(
    description: (I1, Expected) -> String,
    vararg with: Data1<I1, Expected>,
    crossinline body: Suite.(I1, Expected) -> Unit
) {
    with.forEach { (i1, expected) ->
        describe(description(i1, expected)) {
            body(i1, expected)
        }
    }
}

data class Data2<I1, I2, Expected>(
    val i1: I1,
    val i2: I2,
    val expected: Expected
)

inline fun <I1, I2, Expected> data(
    i1: I1, i2: I2,
    expected: Expected
) = Data2(i1, i2, expected)

inline fun <I1, I2, Expected> Suite.data(
    description: (I1, I2) -> String,
    vararg with: Data2<I1, I2, Expected>,
    crossinline body: Suite.(I1, I2, Expected) -> Unit
) {
    with.forEach { (i1, i2, expected) ->
        describe(description(i1, i2)) {
            body(i1, i2, expected)
        }
    }
}

inline fun <I1, I2, Expected> Suite.data(
    description: (I1, I2, Expected) -> String,
    vararg with: Data2<I1, I2, Expected>,
    crossinline body: Suite.(I1, I2, Expected) -> Unit
) {
    with.forEach { (i1, i2, expected) ->
        describe(description(i1, i2, expected)) {
            body(i1, i2, expected)
        }
    }
}

data class Data3<I1, I2, I3, Expected>(
    val i1: I1,
    val i2: I2,
    val i3: I3,
    val expected: Expected
)

inline fun <I1, I2, I3, Expected> data(
    i1: I1, i2: I2, i3: I3,
    expected: Expected
) = Data3(i1, i2, i3, expected)

inline fun <I1, I2, I3, Expected> Suite.data(
    description: (I1, I2, I3) -> String,
    vararg with: Data3<I1, I2, I3, Expected>,
    crossinline body: Suite.(I1, I2, I3, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, expected) ->
        describe(description(i1, i2, i3)) {
            body(i1, i2, i3, expected)
        }
    }
}

inline fun <I1, I2, I3, Expected> Suite.data(
    description: (I1, I2, I3, Expected) -> String,
    vararg with: Data3<I1, I2, I3, Expected>,
    crossinline body: Suite.(I1, I2, I3, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, expected) ->
        describe(description(i1, i2, i3, expected)) {
            body(i1, i2, i3, expected)
        }
    }
}

data class Data4<I1, I2, I3, I4, Expected>(
    val i1: I1,
    val i2: I2,
    val i3: I3,
    val i4: I4,
    val expected: Expected
)

inline fun <I1, I2, I3, I4, Expected> data(
    i1: I1, i2: I2, i3: I3, i4: I4,
    expected: Expected
) = Data4(i1, i2, i3, i4, expected)

inline fun <I1, I2, I3, I4, Expected> Suite.data(
    description: (I1, I2, I3, I4) -> String,
    vararg with: Data4<I1, I2, I3, I4, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, expected) ->
        describe(description(i1, i2, i3, i4)) {
            body(i1, i2, i3, i4, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, Expected> Suite.data(
    description: (I1, I2, I3, I4, Expected) -> String,
    vararg with: Data4<I1, I2, I3, I4, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, expected) ->
        describe(description(i1, i2, i3, i4, expected)) {
            body(i1, i2, i3, i4, expected)
        }
    }
}

data class Data5<I1, I2, I3, I4, I5, Expected>(
    val i1: I1,
    val i2: I2,
    val i3: I3,
    val i4: I4,
    val i5: I5,
    val expected: Expected
)

inline fun <I1, I2, I3, I4, I5, Expected> data(
    i1: I1, i2: I2, i3: I3, i4: I4, i5: I5,
    expected: Expected
) = Data5(i1, i2, i3, i4, i5, expected)

inline fun <I1, I2, I3, I4, I5, Expected> Suite.data(
    description: (I1, I2, I3, I4, I5) -> String,
    vararg with: Data5<I1, I2, I3, I4, I5, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, I5, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, expected) ->
        describe(description(i1, i2, i3, i4, i5)) {
            body(i1, i2, i3, i4, i5, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, Expected> Suite.data(
    description: (I1, I2, I3, I4, I5, Expected) -> String,
    vararg with: Data5<I1, I2, I3, I4, I5, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, I5, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, expected) ->
        describe(description(i1, i2, i3, i4, i5, expected)) {
            body(i1, i2, i3, i4, i5, expected)
        }
    }
}

data class Data6<I1, I2, I3, I4, I5, I6, Expected>(
    val i1: I1,
    val i2: I2,
    val i3: I3,
    val i4: I4,
    val i5: I5,
    val i6: I6,
    val expected: Expected
)

inline fun <I1, I2, I3, I4, I5, I6, Expected> data(
    i1: I1, i2: I2, i3: I3, i4: I4, i5: I5, i6: I6,
    expected: Expected
) = Data6(i1, i2, i3, i4, i5, i6, expected)

inline fun <I1, I2, I3, I4, I5, I6, Expected> Suite.data(
    description: (I1, I2, I3, I4, I5, I6) -> String,
    vararg with: Data6<I1, I2, I3, I4, I5, I6, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, I5, I6, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, expected) ->
        describe(description(i1, i2, i3, i4, i5, i6)) {
            body(i1, i2, i3, i4, i5, i6, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, Expected> Suite.data(
    description: (I1, I2, I3, I4, I5, I6, Expected) -> String,
    vararg with: Data6<I1, I2, I3, I4, I5, I6, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, I5, I6, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, expected) ->
        describe(description(i1, i2, i3, i4, i5, i6, expected)) {
            body(i1, i2, i3, i4, i5, i6, expected)
        }
    }
}

data class Data7<I1, I2, I3, I4, I5, I6, I7, Expected>(
    val i1: I1,
    val i2: I2,
    val i3: I3,
    val i4: I4,
    val i5: I5,
    val i6: I6,
    val i7: I7,
    val expected: Expected
)

inline fun <I1, I2, I3, I4, I5, I6, I7, Expected> data(
    i1: I1, i2: I2, i3: I3, i4: I4, i5: I5, i6: I6, i7: I7,
    expected: Expected
) = Data7(i1, i2, i3, i4, i5, i6, i7, expected)

inline fun <I1, I2, I3, I4, I5, I6, I7, Expected> Suite.data(
    description: (I1, I2, I3, I4, I5, I6, I7) -> String,
    vararg with: Data7<I1, I2, I3, I4, I5, I6, I7, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, I5, I6, I7, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, expected) ->
        describe(description(i1, i2, i3, i4, i5, i6, i7)) {
            body(i1, i2, i3, i4, i5, i6, i7, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, I7, Expected> Suite.data(
    description: (I1, I2, I3, I4, I5, I6, I7, Expected) -> String,
    vararg with: Data7<I1, I2, I3, I4, I5, I6, I7, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, I5, I6, I7, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, expected) ->
        describe(description(i1, i2, i3, i4, i5, i6, i7, expected)) {
            body(i1, i2, i3, i4, i5, i6, i7, expected)
        }
    }
}

data class Data8<I1, I2, I3, I4, I5, I6, I7, I8, Expected>(
    val i1: I1,
    val i2: I2,
    val i3: I3,
    val i4: I4,
    val i5: I5,
    val i6: I6,
    val i7: I7,
    val i8: I8,
    val expected: Expected
)

inline fun <I1, I2, I3, I4, I5, I6, I7, I8, Expected> data(
    i1: I1, i2: I2, i3: I3, i4: I4, i5: I5, i6: I6, i7: I7, i8: I8,
    expected: Expected
) = Data8(i1, i2, i3, i4, i5, i6, i7, i8, expected)

inline fun <I1, I2, I3, I4, I5, I6, I7, I8, Expected> Suite.data(
    description: (I1, I2, I3, I4, I5, I6, I7, I8) -> String,
    vararg with: Data8<I1, I2, I3, I4, I5, I6, I7, I8, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, I5, I6, I7, I8, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, i8, expected) ->
        describe(description(i1, i2, i3, i4, i5, i6, i7, i8)) {
            body(i1, i2, i3, i4, i5, i6, i7, i8, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, I7, I8, Expected> Suite.data(
    description: (I1, I2, I3, I4, I5, I6, I7, I8, Expected) -> String,
    vararg with: Data8<I1, I2, I3, I4, I5, I6, I7, I8, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, I5, I6, I7, I8, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, i8, expected) ->
        describe(description(i1, i2, i3, i4, i5, i6, i7, i8, expected)) {
            body(i1, i2, i3, i4, i5, i6, i7, i8, expected)
        }
    }
}

data class Data9<I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected>(
    val i1: I1,
    val i2: I2,
    val i3: I3,
    val i4: I4,
    val i5: I5,
    val i6: I6,
    val i7: I7,
    val i8: I8,
    val i9: I9,
    val expected: Expected
)

inline fun <I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected> data(
    i1: I1, i2: I2, i3: I3, i4: I4, i5: I5, i6: I6, i7: I7, i8: I8, i9: I9,
    expected: Expected
) = Data9(i1, i2, i3, i4, i5, i6, i7, i8, i9, expected)

inline fun <I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected> Suite.data(
    description: (I1, I2, I3, I4, I5, I6, I7, I8, I9) -> String,
    vararg with: Data9<I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, i8, i9, expected) ->
        describe(description(i1, i2, i3, i4, i5, i6, i7, i8, i9)) {
            body(i1, i2, i3, i4, i5, i6, i7, i8, i9, expected)
        }
    }
}

inline fun <I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected> Suite.data(
    description: (I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected) -> String,
    vararg with: Data9<I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected>,
    crossinline body: Suite.(I1, I2, I3, I4, I5, I6, I7, I8, I9, Expected) -> Unit
) {
    with.forEach { (i1, i2, i3, i4, i5, i6, i7, i8, i9, expected) ->
        describe(description(i1, i2, i3, i4, i5, i6, i7, i8, i9, expected)) {
            body(i1, i2, i3, i4, i5, i6, i7, i8, i9, expected)
        }
    }
}
