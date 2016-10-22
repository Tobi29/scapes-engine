package org.tobi29.scapes.engine.utils.io

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

fun ByteBuffer.asString(): String {
    return String(array(), StandardCharsets.UTF_8)
}

fun ByteBuffer.asArray(): ByteArray {
    val array = ByteArray(remaining())
    get(array)
    return array
}