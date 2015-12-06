package org.tobi29.scapes.engine.input;

import org.tobi29.scapes.engine.utils.Pair;

public class FileType {
    public static final FileType IMAGE = new FileType("*.png", "PNG File");
    public static final FileType MUSIC =
            new FileType("*.ogg", "ogg-Vorbis File", "*.mp3", "MP3 File",
                    "*.wav", "Wave File");
    private final Pair<String, String>[] extensions;

    public FileType(String... extensions) {
        this(extensions(extensions));
    }

    @SafeVarargs
    public FileType(Pair<String, String>... extensions) {
        this.extensions = extensions;
    }

    @SuppressWarnings("unchecked")
    private static Pair<String, String>[] extensions(String[] array) {
        if (array.length % 2 != 0) {
            throw new IllegalArgumentException("Array has to have even length");
        }
        Pair<String, String>[] extensions = new Pair[array.length >> 1];
        for (int i = 0; i < extensions.length; i++) {
            int j = i << 1;
            extensions[i] = new Pair<>(array[j], array[j + 1]);
        }
        return extensions;
    }

    public Pair<String, String>[] extensions() {
        return extensions;
    }
}
