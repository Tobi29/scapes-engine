package org.tobi29.scapes.engine.utils

/* impl */ fun ByteArray.strUTF8() = String(this)

/* impl */ fun String.bytesUTF8() = toByteArray()
