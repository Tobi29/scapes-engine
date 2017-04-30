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

package org.tobi29.scapes.engine.sound

import org.tobi29.scapes.engine.utils.fromBase64
import org.tobi29.scapes.engine.utils.io.ByteBuffer
import org.tobi29.scapes.engine.utils.io.tag.TagBundleResource

// Base64 encoded OGG/Vorbis file
// Original audio by Kenney Vleugels (kenney.nl)
val CLICK = TagBundleResource(ByteBuffer.wrap("""
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
1VRV2xZNVbYlTRNNTfRUVRNFVRVV05ZNVbVtzzRl2VRV3RZV1bZl2xZ+V5Z13zNNWRZV1dZNVbV115Z9
X9ZtXZg0zTQ1UVRVTRRV1VRV2zZV17Y1UXRVUVVlWVRVWXZlWfdVV9Z9SxRV1VNN2RVVVbZV2fVtVZZ9
4XRVXVdl2fdVWRZ+W9eF4fZ94RhV1dZN19V1VZZ9YdZlYbd13yhpmmlqoqiqmiiqqqmqtm2qrq1bouiq
oqrKsmeqrqzKsq+rrmzrmiiqrqiqsiyqqiyrsqz7qizrtqiquq3KsrCbrqvrtu8LwyzrunCqrq6rsuz7
qizruq3rxnHrujB8pinLpqvquqm6um7runHMtm0co6rqvirLwrDKsu/rui+0dSFRVXXdlF3jV2VZ921f
d55b94WybTu/rfvKceu60vg5z28cubZtHLNuG7+t+8bzKz9hOI6lZ5q2baqqrZuqq+uybivDrOtCUVV9
XZVl3zddWRdu3zeOW9eNoqrquirLvrDKsjHcxm8cuzAcXds2jlvXnbKtC31jyPcJz2vbxnH7OuP2daOv
DAnHjwAAgAEHAIAAE8pAoSErAoA4AQAGIecUUxAqxSB0EFLqIKRUMQYhc05KxRyUUEpqIZTUKsYgVI5J
yJyTEkpoKZTSUgehpVBKa6GU1lJrsabUYu0gpBZKaS2U0lpqqcbUWowRYxAy56RkzkkJpbQWSmktc05K
56CkDkJKpaQUS0otVsxJyaCj0kFIqaQSU0mptVBKa6WkFktKMbYUW24x1hxKaS2kEltJKcYUU20txpoj
xiBkzknJnJMSSmktlNJa5ZiUDkJKmYOSSkqtlZJSzJyT0kFIqYOOSkkptpJKTKGU1kpKsYVSWmwx1pxS
bDWU0lpJKcaSSmwtxlpbTLV1EFoLpbQWSmmttVZraq3GUEprJaUYS0qxtRZrbjHmGkppraQSW0mpxRZb
ji3GmlNrNabWam4x5hpbbT3WmnNKrdbUUo0txppjbb3VmnvvIKQWSmktlNJiai3G1mKtoZTWSiqxlZJa
bDHm2lqMOZTSYkmpxZJSjC3GmltsuaaWamwx5ppSi7Xm2nNsNfbUWqwtxppTS7XWWnOPufVWAADAgAMA
QIAJZaDQkJUAQBQAAEGIUs5JaRByzDkqCULMOSepckxCKSlVzEEIJbXOOSkpxdY5CCWlFksqLcVWaykp
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
dY7fWIJkOj4aAEIRAAAAAAAAdXYmAAAA
""".replace("\n","").fromBase64()))