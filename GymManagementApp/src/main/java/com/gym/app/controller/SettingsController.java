package com.gym.app.controller;

import com.gym.app.MainApp;
import com.gym.app.util.ErrorLogger;
import com.gym.app.util.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private ComboBox<LanguageOption> languageComboBox;

    private ResourceBundle currentBundle;

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
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
