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

// GENERATED FILE, DO NOT EDIT DIRECTLY!!!
// Generation script can be found in `resources/codegen/GenStringDataJVM.kts`.
// Run `resources/codegen/codegen.sh` to update sources.

package com.j256.simplemagik

private val ianaFont0 get() = "collection,font/collection,[RFC8081]\notf,font/otf,[RFC8081]\nsfnt,font/sfnt,[RFC8081]\nttf,font/ttf,[RFC8081]\nwoff,font/woff,[RFC8081]\nwoff2,font/woff2,[RFC8081]"

internal actual val ianaFont = sequenceOf(
    ianaFont0
).flatMap { it.splitToSequence('\n') }
