package org.tobi29.scapes.engine.utils

actual fun checkPermission(permission: String) {
    val security: SecurityManager? = System.getSecurityManager()
    security?.checkPermission(RuntimePermission(permission))
}
