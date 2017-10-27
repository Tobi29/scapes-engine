package org.tobi29.scapes.engine.utils.tag

import java.math.BigDecimal
import java.math.BigInteger

actual internal fun convertNumberToType(type: Number,
                                      convert: Number): Number {
    return when (type) {
        is Byte -> convert.toByte()
        is Short -> convert.toShort()
        is Int -> convert.toInt()
        is Long -> convert.toLong()
        is Float -> convert.toFloat()
        is Double -> convert.toDouble()
        is BigInteger -> try {
            BigInteger(convert.toString())
        } catch(e: NumberFormatException) {
            BigDecimal(convert.toString()).toBigInteger()
        }
        is BigDecimal -> BigDecimal(convert.toString())
        else -> throw IllegalArgumentException(
                "Invalid number type: ${type::class}")
    }
}
