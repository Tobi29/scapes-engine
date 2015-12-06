package org.tobi29.scapes.engine.utils.io.filesystem;

import java.net.URI;

public interface FilePath extends Path, Comparable<FilePath> {
    URI toUri();

    FilePath resolve(String other);

    FilePath resolve(FilePath other);

    FilePath getFileName();

    FilePath toAbsolutePath();
}
