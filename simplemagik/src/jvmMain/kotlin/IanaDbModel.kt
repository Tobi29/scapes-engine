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

private val ianaModel0 get() = "3mf,model/3mf,[http://www.3mf.io/specification][_3MF][Michael_Sweet]\nexample,model/example,[RFC4735]\ngltf-binary,model/gltf-binary,[Khronos][Saurabh_Bhatia]\ngltf+json,model/gltf+json,[Khronos][Uli_Klumpp]\niges,model/iges,[Curtis_Parks]\nmesh,,[RFC2077]\nstl,model/stl,[DICOM_Standards_Committee][Lisa_Spellman]\nvnd.collada+xml,model/vnd.collada+xml,[James_Riordon]\nvnd.dwf,model/vnd-dwf,[Jason_Pratt]\nvnd.flatland.3dml,model/vnd.flatland.3dml,[Michael_Powers]\nvnd.gdl,model/vnd.gdl,[Attila_Babits]\nvnd.gs-gdl,model/vnd.gs-gdl,[Attila_Babits]\nvnd.gtw,model/vnd.gtw,[Yutaka_Ozaki]\nvnd.moml+xml,model/vnd.moml+xml,[Christopher_Brooks]\nvnd.mts,model/vnd.mts,[Boris_Rabinovitch]\nvnd.opengex,model/vnd.opengex,[Eric_Lengyel]\nvnd.parasolid.transmit.binary,model/vnd.parasolid.transmit-binary,[Parasolid]\nvnd.parasolid.transmit.text,model/vnd.parasolid.transmit-text,[Parasolid]\nvnd.rosette.annotated-data-model,model/vnd.rosette.annotated-data-model,[Benson_Margulies]\nvnd.valve.source.compiled-map,model/vnd.valve.source.compiled-map,[Henrik_Andersson]\nvnd.vtu,model/vnd.vtu,[Boris_Rabinovitch]\nvrml,,[RFC2077]\nx3d-vrml,model/x3d-vrml,[Web3D][Web3D_X3D]\nx3d+fastinfoset,model/x3d+fastinfoset,[Web3D_X3D]\nx3d+xml,model/x3d+xml,[Web3D][Web3D_X3D]"

internal actual val ianaModel = sequenceOf(
    ianaModel0
).flatMap { it.splitToSequence('\n') }
