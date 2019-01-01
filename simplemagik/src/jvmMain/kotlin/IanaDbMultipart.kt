/*
 * Copyright 2012-2019 Tobi29
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

private val ianaMultipart0 get() = "alternative,,[RFC2046][RFC2045]\nappledouble,multipart/appledouble,[Patrik_Faltstrom]\nbyteranges,multipart/byteranges,[RFC7233]\ndigest,,[RFC2046][RFC2045]\nencrypted,multipart/encrypted,[RFC1847]\nexample,multipart/example,[RFC4735]\nform-data,multipart/form-data,[RFC7578]\nheader-set,multipart/header-set,[Dave_Crocker]\nmixed,,[RFC2046][RFC2045]\nmultilingual,multipart/multilingual,[RFC8255]\nparallel,,[RFC2046][RFC2045]\nrelated,multipart/related,[RFC2387]\nreport,multipart/report,[RFC6522]\nsigned,multipart/signed,[RFC1847]\nvnd.bint.med-plus,multipart/vnd.bint.med-plus,[Heinz-Peter_Schütz]\nvoice-message,multipart/voice-message,[RFC3801]\nx-mixed-replace,multipart/x-mixed-replace,[W3C][Robin_Berjon]"

internal actual val ianaMultipart = sequenceOf(
    ianaMultipart0
).flatMap { it.splitToSequence('\n') }
