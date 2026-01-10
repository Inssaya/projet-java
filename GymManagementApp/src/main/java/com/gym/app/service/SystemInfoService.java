package com.gym.app.service;

import com.gym.app.db.DatabaseManager;
import com.gym.app.util.ErrorLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SystemInfoService {

    private static final String LICENSE_KEY = "hardware_hash";

    /**
     * Retrieves the stored hardware hash from the system_info table.
     * @return The stored hash, or null if not found.
     */
    public String getStoredHardwareHash() {
        String sql = "SELECT value FROM system_info WHERE key = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LICENSE_KEY);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to retrieve stored hardware hash.");
        }
        return null;
    }

    /**
     * Stores the generated hardware hash in the system_info table.
     * @param hash The hardware hash to store.
     * @return true if successful, false otherwise.
     */
    public boolean storeHardwareHash(String hash) {
        String sql = "INSERT OR REPLACE INTO system_info (key, value) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LICENSE_KEY);
            pstmt.setString(2, hash);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            ErrorLogger.log(e, "Failed to store hardware hash.");
            return false;
        }
    }

    /**
     * Generates a pseudo-hardware ID hash.
     * NOTE: A real-world application would use platform-specific methods
     * to get MAC address, CPU serial, etc. For a cross-platform Java
     * desktop app in a sandboxed environment, we use a simple UUID
     * as a placeholder for the "PC hardware ID binding license system".
     * @return A unique string representing the "hardware ID".
     */
    public String generateHardwareHash() {
        // In a real application, this would be a complex hash of hardware identifiers.
        // For simulation, we use a fixed UUID based on the environment.
        // Since the sandbox environment is persistent, this will be consistent.
        String uniqueID = System.getProperty("user.name") + System.getProperty("os.name") + System.getProperty("os.version");
        return UUID.nameUUIDFromBytes(uniqueID.getBytes()).toString();
    }

    /**
     * Checks the license status.
     * @return true if the license is valid (first run or hash matches), false otherwise.
     */
    public boolean checkLicense() {
        String storedHash = getStoredHardwareHash();
        String currentHash = generateHardwareHash();

        if (storedHash == null) {
            // First run: Store the current hash
            System.out.println("First run detected. Storing hardware hash: " + currentHash);
            return storeHardwareHash(currentHash);
        } else {
            // Subsequent runs: Compare hashes
            if (storedHash.equals(currentHash)) {
                System.out.println("License check successful. Hardware hash matches.");
                return true;
            } else {
                System.err.println("License check failed. Hardware hash mismatch.");
                return false;
            }
        }
    }
}
