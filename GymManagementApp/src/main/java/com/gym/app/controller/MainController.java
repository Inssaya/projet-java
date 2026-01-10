package com.gym.app.controller;

import com.gym.app.util.ViewManager;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import java.util.Locale;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private StackPane contentPane; // The area where different views will be loaded
    @FXML
    private BorderPane mainRoot;
    @FXML
    private HBox languageFlagsBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set the content pane for the ViewManager
        ViewManager.setMainContentPane(contentPane);
        
        // Pass the MainController instance to ViewManager for view cleanup
        ViewManager.setMainController(this);
        
        // Load the initial view (Dashboard)
        ViewManager.loadView("Dashboard.fxml");
        
        // Handle language-specific UI setup
        setupLanguageUI(rb);
    }

    private void setupLanguageUI(ResourceBundle rb) {
        Locale currentLocale = rb.getLocale();
        
        // 1. Handle RTL layout for Arabic
        if (currentLocale.getLanguage().equals("ar")) {
            mainRoot.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
            // Apply Cairo font if needed via CSS
        } else {
            mainRoot.setNodeOrientation(javafx.geometry.NodeOrientation.LEFT_TO_RIGHT);
        }
        
        // 2. Setup Language Flags
        languageFlagsBox.getChildren().clear();
        
        // English
        Button enFlag = createFlagButton("🇬🇧", new Locale("en", "US"), "English");
        // French
        Button frFlag = createFlagButton("🇫🇷", new Locale("fr", "FR"), "Français");
        // Arabic
        Button arFlag = createFlagButton("🇲🇦", new Locale("ar", "MA"), "العربية");
        
        languageFlagsBox.getChildren().addAll(enFlag, frFlag, arFlag);
    }
    
    private Button createFlagButton(String flagEmoji, Locale locale, String tooltipText) {
        Button button = new Button(flagEmoji);
        button.setTooltip(new Tooltip(tooltipText));
        button.setOnAction(e -> handleLanguageSwitch(locale));
        button.setStyle("-fx-font-size: 18px; -fx-background-color: transparent; -fx-padding: 0;");
        return button;
    }
    
    private void handleLanguageSwitch(Locale locale) {
        // This will be handled by the SettingsController's reload logic, 
        // so we just open the settings page.
        ViewManager.loadView("Settings.fxml");
    }
    
    // Unused parameter - keeping for FXML compatibility

    // Navigation methods (to be linked to sidebar buttons in FXML)
    @FXML
    private void handleDashboardClick() {
        ViewManager.loadView("Dashboard.fxml");
    }

    @FXML
    private void handleMemberManagementClick() {
        ViewManager.loadView("MemberManagement.fxml");
    }

    @FXML
    private void handleCheckInClick() {
        ViewManager.loadView("CheckIn.fxml");
    }
    
    // Method to handle controller cleanup when view is switched
    public void cleanupController(Object controller) {
        if (controller instanceof CheckInController checkInController) {
            checkInController.shutdown();
            System.out.println("CheckInController shut down.");
        }
        // Add other controller cleanup logic here if needed
    }

    @FXML
    private void handleReportsClick() {
        ViewManager.loadView("Reports.fxml");
    }

    @FXML
    private void handleSettingsClick() {
        ViewManager.loadView("Settings.fxml");
    }
}
