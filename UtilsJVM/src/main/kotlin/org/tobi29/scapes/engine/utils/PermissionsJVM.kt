package org.tobi29.scapes.engine.utils

impl fun checkPermission(permission: String) {
    val security: SecurityManager? = System.getSecurityManager()
    security?.checkPermission(RuntimePermission(permission))
}
