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

#!/usr/bin/kotlinc -script
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

val pkg = args[0]
val name = args[1]

val batches = ArrayList<String>()

val currentBatch = StringBuilder()
readLine() // Skip first line
while (true) {
    val line = readLine() ?: break
    if (currentBatch.isNotEmpty()) currentBatch.append('\n')
    currentBatch.append(line)
    if (currentBatch.length >= 4096) {
        batches.add(currentBatch.toString())
        currentBatch.setLength(0)
    }
}
if (currentBatch.isNotEmpty()) {
    batches.add(currentBatch.toString())
}

fun escapeString(str: String): String = str
    .replace("\\", "\\\\")
    .replace("\n", "\\n")
    .replace("\t", "\\t")
    .replace("\"", "\\\"")
    .replace("\$", "\\\$")

println(
    """/*
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

package $pkg
"""
)

for (i in batches.indices) {
    println("private val $name$i get() = \"${escapeString(batches[i])}\"\n")
}

println(
    """internal actual val $name = sequenceOf(
    ${batches.indices.joinToString(",\n    ") { "$name$it" }}
).flatMap { it.splitToSequence('\n') }"""
)
