package org.tobi29.scapes.engine.platform

impl internal fun environmentVariableImpl(key: String): String? =
        System.getenv(key)
