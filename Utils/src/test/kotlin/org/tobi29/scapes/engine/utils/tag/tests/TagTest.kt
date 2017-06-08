/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.utils.tag.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.tag.TagMap
import org.tobi29.scapes.engine.utils.tag.toTag
import org.tobi29.scapes.engine.utils.tag.toUUID
import org.tobi29.scapes.engine.utils.toUUID
import kotlin.collections.set

object TagTests : Spek({
    describe("getting a uuid from a tag") {
        given("a tag with a valid string uuid") {
            val tag = "2e68827e-820c-40bf-8b92-3553bbc62a07".toTag()
            it("should return an uuid") {
                tag.toUUID() shouldEqual "2e68827e-820c-40bf-8b92-3553bbc62a07".toUUID()
            }
        }
        given("a tag with a most and least number") {
            val tag = TagMap {
                this["Most"] = 3344066203181924543.toTag()
                this["Least"] = (-8389584522088928761).toTag()
            }
            it("should return an uuid") {
                tag.toUUID() shouldEqual "2e68827e-820c-40bf-8b92-3553bbc62a07".toUUID()
            }
        }
    }
})
