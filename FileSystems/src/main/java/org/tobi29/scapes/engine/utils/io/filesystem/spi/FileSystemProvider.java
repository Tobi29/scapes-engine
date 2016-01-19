package org.tobi29.scapes.engine.utils.io.filesystem.spi;

import org.tobi29.scapes.engine.utils.io.filesystem.FileUtilImpl;

public interface FileSystemProvider {
    boolean available();

    FileUtilImpl implementation();
}
