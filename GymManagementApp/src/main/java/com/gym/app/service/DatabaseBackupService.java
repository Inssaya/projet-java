package com.gym.app.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.gym.app.db.DatabaseManager;
import com.gym.app.util.AppPaths;

public class DatabaseBackupService {

    public Path getDatabasePath() {
        return AppPaths.getDatabasePath();
    }

    public void backupTo(Path destination) throws IOException {
        if (destination == null) {
            throw new IllegalArgumentException("destination cannot be null");
        }

        Path dbPath = getDatabasePath();
        if (!Files.exists(dbPath)) {
            // Ensure DB exists by initializing schema (SQLite creates the file on connect).
            DatabaseManager.initializeDatabase();
        }

        Path parent = destination.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.copy(dbPath, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }

    public void restoreFrom(Path source) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }

        if (!Files.exists(source)) {
            throw new IOException("Backup file not found: " + source);
        }

        Path dbPath = getDatabasePath();
        Files.createDirectories(dbPath.getParent());

        Files.copy(source, dbPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }
}
