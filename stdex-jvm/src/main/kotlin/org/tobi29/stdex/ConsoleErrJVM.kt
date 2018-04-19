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

package org.tobi29.stdex

/** Prints the given message to the standard error stream. */
inline fun printerr(message: Int) {
    System.err.print(message)
}

/** Prints the given message to the standard error stream. */
inline fun printerr(message: Long) {
    System.err.print(message)
}

/** Prints the given message to the standard error stream. */
inline fun printerr(message: Byte) {
    System.err.print(message)
}

/** Prints the given message to the standard error stream. */
inline fun printerr(message: Short) {
    System.err.print(message)
}

/** Prints the given message to the standard error stream. */
inline fun printerr(message: Char) {
    System.err.print(message)
}

/** Prints the given message to the standard error stream. */
inline fun printerr(message: Boolean) {
    System.err.print(message)
}

/** Prints the given message to the standard error stream. */
inline fun printerr(message: Float) {
    System.err.print(message)
}

/** Prints the given message to the standard error stream. */
inline fun printerr(message: Double) {
    System.err.print(message)
}

/** Prints the given message to the standard error stream. */
inline fun printerr(message: CharArray) {
    System.err.print(message)
}

/** Prints the given message and newline to the standard error stream. */
inline fun printerrln(message: Int) {
    System.err.println(message)
}

/** Prints the given message and newline to the standard error stream. */
inline fun printerrln(message: Long) {
    System.err.println(message)
}

/** Prints the given message and newline to the standard error stream. */
inline fun printerrln(message: Byte) {
    System.err.println(message)
}

/** Prints the given message and newline to the standard error stream. */
inline fun printerrln(message: Short) {
    System.err.println(message)
}

/** Prints the given message and newline to the standard error stream. */
inline fun printerrln(message: Char) {
    System.err.println(message)
}

/** Prints the given message and newline to the standard error stream. */
inline fun printerrln(message: Boolean) {
    System.err.println(message)
}

/** Prints the given message and newline to the standard error stream. */
inline fun printerrln(message: Float) {
    System.err.println(message)
}

/** Prints the given message and newline to the standard error stream. */
inline fun printerrln(message: Double) {
    System.err.println(message)
}

/** Prints the given message and newline to the standard error stream. */
inline fun printerrln(message: CharArray) {
    System.err.println(message)
}

actual inline fun printerr(message: Any?) {
    System.err.print(message)
}

actual inline fun printerrln(message: Any?) {
    System.err.println(message)
}

actual inline fun printerrln() {
    System.err.println()
}
