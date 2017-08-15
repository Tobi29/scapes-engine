package org.tobi29.scapes.engine.chrono

/**
 * UTC time zone, behaves the same across all systems
 */
header val timeZoneUTC: TimeZone

/**
 * Local system time zone
 */
header val timeZoneLocal: TimeZone

/**
 * Time zone for given name
 * @param name Name of timezone, currently implementation dependant
 * @return Time zone handle
 */
header fun timeZoneOf(name: String): TimeZone
