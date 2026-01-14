package com.gym.app.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import com.gym.app.MainApp;
import com.gym.app.db.DatabaseManager;
import com.gym.app.service.DatabaseBackupService;
import com.gym.app.util.ErrorLogger;
import com.gym.app.util.ViewManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SettingsController implements Initializable {

    @FXML
    private ComboBox<LanguageOption> languageComboBox;

    private ResourceBundle currentBundle;

    private final DatabaseBackupService backupService = new DatabaseBackupService();

    private static class LanguageOption {
        String name;
        Locale locale;

        public LanguageOption(String name, Locale locale) {
            this.name = name;
            this.locale = locale;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.currentBundle = rb;
        
        ObservableList<LanguageOption> languages = FXCollections.observableArrayList(
                new LanguageOption("English 🇬🇧", new Locale("en", "US")),
                new LanguageOption("Français 🇫🇷", new Locale("fr", "FR")),
                new LanguageOption("العربية 🇲🇦 (RTL)", new Locale("ar", "MA"))
        );
        
        languageComboBox.setItems(languages);
        
        // Select the current language
        Locale currentLocale = rb.getLocale();
        for (LanguageOption option : languages) {
            if (option.locale.getLanguage().equals(currentLocale.getLanguage())) {
                languageComboBox.getSelectionModel().select(option);
                break;
            }
        }
    }

    @FXML
    private void handleApplyLanguage() {
        LanguageOption selected = languageComboBox.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a language.");
            return;
        }

        try {
            // Load the new resource bundle
            ResourceBundle newBundle = ResourceBundle.getBundle("com.gym.app.i18n.messages", selected.locale);
            
            // Update the ViewManager's bundle
            ViewManager.setResourceBundle(newBundle);
            
            // Reload the main application stage to apply the new language
            Stage stage = (Stage) languageComboBox.getScene().getWindow();
            
            // Re-run the start method logic to reload all FXML with the new bundle
            // This is a common way to handle full UI language change in JavaFX
            MainApp mainApp = new MainApp();
            mainApp.start(stage);
            
            // NOTE: For RTL support, we would need to apply a specific CSS class or property
            // to the root of the MainLayout.fxml based on the selected language (e.g., Arabic).
            // This is a placeholder for that logic.
            if (selected.locale.getLanguage().equals("ar")) {
                // Apply RTL styling (e.g., load a specific RTL CSS or set node properties)
                // For now, we'll rely on the FXML/CSS to handle it if possible.
                System.out.println("RTL language selected. Custom styling may be required.");
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Language applied successfully. The application has been reloaded.");

        } catch (Exception e) {
            ErrorLogger.log(e, "Failed to apply new language settings.");
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to apply language. Check error log.");
        }
    }

    @FXML
    private void handleBackupDatabase() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(getText("settings.db.fileChooser.backup", "Choose backup file location"));
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    getText("settings.db.fileChooser.filter", "SQLite Database (*.db, *.sqlite)"),
                    "*.db", "*.sqlite"
            ));
            chooser.setInitialFileName("gym-backup-" + LocalDate.now() + ".db");

            File destination = chooser.showSaveDialog(languageComboBox.getScene().getWindow());
            if (destination == null) {
                return;
            }

            backupService.backupTo(destination.toPath());
            showAlert(Alert.AlertType.INFORMATION,
                    getText("alert.success.title", "Success"),
                    getText("settings.db.backup.success", "Backup created successfully."));

        } catch (Exception e) {
            ErrorLogger.log(e, "Failed to backup SQLite database.");
            showAlert(Alert.AlertType.ERROR,
                    getText("alert.error.title", "Error"),
                    getText("settings.db.backup.error", "Failed to create backup. Check error log."));
        }
    }

    @FXML
    private void handleRestoreDatabase() {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle(getText("settings.db.restore.title", "Restore Database"));
            confirm.setHeaderText(null);
            confirm.setContentText(getText("settings.db.restore.confirm", "Restoring will overwrite current data. Continue?"));
            Optional<ButtonType> choice = confirm.showAndWait();
            if (choice.isEmpty() || choice.get() != ButtonType.OK) {
                return;
            }

            FileChooser chooser = new FileChooser();
            chooser.setTitle(getText("settings.db.fileChooser.restore", "Select a backup file to restore"));
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    getText("settings.db.fileChooser.filter", "SQLite Database (*.db, *.sqlite)"),
                    "*.db", "*.sqlite"
            ));

            File source = chooser.showOpenDialog(languageComboBox.getScene().getWindow());
            if (source == null) {
                return;
            }

            // Best-effort: restore by copying over the SQLite file.
            // If the DB is locked (common on Windows), we show a helpful message.
            Path dbPath = backupService.getDatabasePath();
            backupService.restoreFrom(source.toPath());

            // Re-initialize schema in case the restored DB is older/newer.
            DatabaseManager.initializeDatabase();

            showAlert(Alert.AlertType.INFORMATION,
                    getText("alert.success.title", "Success"),
                    getText("settings.db.restore.success", "Database restored successfully. Please restart the application."));

            System.out.println("Database restored to: " + dbPath);

        } catch (Exception e) {
            ErrorLogger.log(e, "Failed to restore SQLite database.");
            showAlert(Alert.AlertType.ERROR,
                    getText("alert.error.title", "Error"),
                    getText("settings.db.restore.error", "Restore failed. If the database is in use, close the app and try again."));
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String getText(String key, String fallback) {
        if (currentBundle == null) {
            return fallback;
        }
        try {
            return currentBundle.getString(key);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
