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

private val ianaMessage0 get() = "CPIM,message/CPIM,[RFC3862]\ndelivery-status,message/delivery-status,[RFC1894]\ndisposition-notification,message/disposition-notification,[RFC8098]\nexample,message/example,[RFC4735]\nexternal-body,,[RFC2045][RFC2046]\nfeedback-report,message/feedback-report,[RFC5965]\nglobal,message/global,[RFC6532]\nglobal-delivery-status,message/global-delivery-status,[RFC6533]\nglobal-disposition-notification,message/global-disposition-notification,[RFC6533]\nglobal-headers,message/global-headers,[RFC6533]\nhttp,message/http,[RFC7230]\nimdn+xml,message/imdn+xml,[RFC5438]\nnews - OBSOLETED by RFC5537,message/news,[RFC5537][Henry_Spencer]\npartial,,[RFC2045][RFC2046]\nrfc822,,[RFC2045][RFC2046]\ns-http,message/s-http,[RFC2660]\nsip,message/sip,[RFC3261]\nsipfrag,message/sipfrag,[RFC3420]\ntracking-status,message/tracking-status,[RFC3886]\nvnd.si.simp - OBSOLETED by request,message/vnd.si.simp,[Nicholas_Parks_Young]\nvnd.wfa.wsc,message/vnd.wfa.wsc,[Mick_Conley]"

internal actual val ianaMessage = sequenceOf(
    ianaMessage0
).flatMap { it.splitToSequence('\n') }
