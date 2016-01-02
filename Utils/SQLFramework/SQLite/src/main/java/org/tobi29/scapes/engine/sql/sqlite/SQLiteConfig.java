package org.tobi29.scapes.engine.sql.sqlite;

public class SQLiteConfig {
    public boolean secureDelete = true;
    public JournalMode journalMode = JournalMode.DELETE;
    public Synchronous synchronous = Synchronous.FULL;

    public enum JournalMode {
        DELETE,
        TUNCATE,
        PERSIST,
        MEMORY,
        WAL,
        OFF
    }

    public enum Synchronous {
        OFF,
        NORMAL,
        FULL
    }
}
