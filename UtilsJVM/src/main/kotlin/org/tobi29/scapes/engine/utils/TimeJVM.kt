package org.tobi29.scapes.engine.utils

actual val systemClock = object : Clock {
    override fun timeMillis(): InstantMillis =
            System.currentTimeMillis()

    override fun timeNanos(): InstantNanos =
            timeMillis().toInt128() * 1000000L.toInt128()
}

actual val steadyClock = object : SteadyClock {
    override fun timeSteadyNanos(): InstantSteadyNanos =
            System.nanoTime()
}
