package org.tobi29.scapes.engine.platform

actual internal fun environmentVariableImpl(key: String): String? =
        System.getenv(key)
