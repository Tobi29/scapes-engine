/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils.io;

import java.io.IOException;
import java.io.OutputStream;

public class ByteStreamOutputStream extends OutputStream {
    private final WritableByteStream stream;

    public ByteStreamOutputStream(WritableByteStream stream) {
        this.stream = stream;
    }

    @Override
    public void write(int b) throws IOException {
        stream.put(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        stream.put(b, off, len);
    }
}
