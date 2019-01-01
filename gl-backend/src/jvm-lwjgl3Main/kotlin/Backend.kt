/*
 * Copyright 2012-2018 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.scapes.engine.backends.opengl

import net.gitout.ktbindings.gl.*
import org.tobi29.io.tag.ReadTagMutableMap
import org.tobi29.io.tag.toBoolean
import org.tobi29.io.tag.toMap
import org.tobi29.logging.KLogger
import org.tobi29.platform.PLATFORM
import org.tobi29.platform.Platform
import org.tobi29.scapes.engine.Container
import org.tobi29.scapes.engine.GLBackend
import org.tobi29.scapes.engine.graphics.GL

object GLBackendLWJGL : GLBackend {
    override fun createGL(container: Container): GL =
        GLImpl(::contextGL43, GL11::delete, container::isRenderCall)

    override fun requestLegacy(config: ReadTagMutableMap): String? {
        val tagMap = config["Compatibility"]?.toMap()
        return workaroundLegacyProfile(tagMap)
    }
}

private fun workaroundLegacyProfile(tagMap: ReadTagMutableMap?): String? {
    val gl = contextGL43()
    try {
        if (tagMap?.get("ForceLegacyGL")?.toBoolean() == true) {
            logger.warn { "Forcing a legacy profile, this is unsupported!" }
            return "Forced by config"
        }
        if (tagMap?.get("ForceCoreGL")?.toBoolean() == true) {
            logger.warn { "Forcing a core profile, this is unsupported!" }
            return null
        }
        val vendor = gl.glGetString(GL_VENDOR)
        // AMD Catalyst/Crimson driver on both Linux and MS Windows ©
        // causes JVM crashes in glDrawArrays and glDrawElements without
        // any obvious reason to why, using a legacy context appears to
        // fully get rid of those crashes, so this might be a driver bug
        // as this does not happen on any other driver
        // Note: This does not affect the macOS driver or radeonsi
        // Note: Untested with AMDGPU-Pro
        if ((PLATFORM == Platform.LINUX || PLATFORM == Platform.WINDOWS) &&
            vendor == "ATI Technologies Inc.") {
            // AMD is bloody genius with their names
            return "Crashes on AMD Radeon Software Crimson"
        }
        return null
    } finally {
        gl.delete()
    }
}

private val logger = KLogger<GLBackendLWJGL>()
