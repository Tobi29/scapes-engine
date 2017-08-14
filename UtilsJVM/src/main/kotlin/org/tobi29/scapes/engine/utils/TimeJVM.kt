package org.tobi29.scapes.engine.utils

impl val systemClock = object : Clock {
    override fun timeMillis(): InstantMillis =
            System.currentTimeMillis()

    override fun timeNanos(): InstantNanos =
            timeMillis().toInt128() * 1000000L.toInt128()
}

impl val steadyClock = object : SteadyClock {
    override fun timeSteadyNanos(): InstantSteadyNanos =
            System.nanoTime()
}
