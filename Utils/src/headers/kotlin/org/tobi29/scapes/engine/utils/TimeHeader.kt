package org.tobi29.scapes.engine.utils

/**
 * System clock for retrieving time as used by the system
 *
 * Origin shall be same as usual on unix systems and timezone must be UT or UTC
 */
header val systemClock: Clock

/**
 * Monotonically increasing clock with best effort precision
 */
header val steadyClock: SteadyClock
