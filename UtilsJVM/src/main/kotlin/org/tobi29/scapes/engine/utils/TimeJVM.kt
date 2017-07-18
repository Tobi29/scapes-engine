package org.tobi29.scapes.engine.utils

impl val systemClock = object : Clock {
    override fun timeMillis(): InstantMillis =
            System.currentTimeMillis()

    override fun timeNanos(): InstantNanos =
            System.nanoTime()
}
