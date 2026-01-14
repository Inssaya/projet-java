package com.gym.app.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppPaths {

    private static final String WINDOWS_APP_DIR = "GymApp";
    private static final String MAC_APP_DIR = "GymApp";
    private static final String LINUX_APP_DIR = "gymapp";

    private static final String DB_FILE_NAME = "gym.db";
    private static final String LEGACY_DB_FILE_NAME = "gym_management.db";

    private static final String LOGS_DIR_NAME = "logs";
    private static final String QR_DIR_NAME = "qrcodes";
    private static final String REPORTS_DIR_NAME = "reports";

    private static final String ERROR_LOG_FILE_NAME = "error_log.txt";

    private AppPaths() {
        // Utility class
    }

    public static Path getAppDataDir() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        String userHome = System.getProperty("user.home", ".");

        if (osName.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData == null || localAppData.isBlank()) {
                // Fallback for unusual environments
                localAppData = Paths.get(userHome, "AppData", "Local").toString();
            }
            return Paths.get(localAppData, WINDOWS_APP_DIR);
        }

        if (osName.contains("mac")) {
            return Paths.get(userHome, "Library", "Application Support", MAC_APP_DIR);
        }

        // Linux / Unix
        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.isBlank()) {
            return Paths.get(xdgDataHome, LINUX_APP_DIR);
        }
        return Paths.get(userHome, ".local", "share", LINUX_APP_DIR);
    }

    public static Path getDatabasePath() {
        Path dir = getAppDataDir();
        ensureDirectoryExists(dir);
        return dir.resolve(DB_FILE_NAME);
    }

    public static Path getLogsDir() {
        Path dir = getAppDataDir().resolve(LOGS_DIR_NAME);
        ensureDirectoryExists(dir);
        return dir;
    }

    public static Path getErrorLogPath() {
        return getLogsDir().resolve(ERROR_LOG_FILE_NAME);
    }

    public static Path getQrCodesDir() {
        Path dir = getAppDataDir().resolve(QR_DIR_NAME);
        ensureDirectoryExists(dir);
        return dir;
    }

    public static Path getReportsDir() {
        Path dir = getAppDataDir().resolve(REPORTS_DIR_NAME);
        ensureDirectoryExists(dir);
        return dir;
    }

    public static Path getLegacyDatabasePathInWorkingDir() {
        return Paths.get(LEGACY_DB_FILE_NAME).toAbsolutePath().normalize();
    }

    public static void migrateLegacyDatabaseIfPresent() {
        Path legacy = getLegacyDatabasePathInWorkingDir();
        Path target = getDatabasePath();

        if (Files.exists(target)) {
            return;
        }

        if (!Files.exists(legacy)) {
            return;
        }

        try {
            Files.copy(legacy, target);
        } catch (IOException e) {
            // Best-effort migration; if it fails, the app will create a new DB file.
            System.err.println("Failed to migrate legacy database file to application data directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void ensureDirectoryExists(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("Failed to create application data directory: " + dir + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }
}
