package com.gym.app;

import com.gym.app.db.DatabaseManager;
import com.gym.app.util.ErrorLogger;
import com.gym.app.util.ViewManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import com.gym.app.service.SystemInfoService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Initialize Database
            DatabaseManager.initializeDatabase();

            // 2. License Protection Check
            SystemInfoService licenseService = new SystemInfoService();
            if (!licenseService.checkLicense()) {
                // If mismatch → App must exit with message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Access Denied");
                alert.setHeaderText("License Mismatch Error");
                alert.setContentText("Access Denied: This license is bound to another computer.");
                alert.showAndWait();
                Platform.exit();
                return;
            }

            // 3. Set up Resource Bundle for Multi-Language Support (Default to English)
            Locale defaultLocale = new Locale("en", "US");
            ResourceBundle bundle = ResourceBundle.getBundle("com.gym.app.i18n.messages", defaultLocale);
            ViewManager.setResourceBundle(bundle);

            // 3. Load the Main Application Layout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gym/app/view/MainLayout.fxml"), bundle);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            // Store the resource bundle in the scene properties
            scene.getProperties().put("resources", bundle);
            
            primaryStage.setTitle(bundle.getString("app.title"));
            primaryStage.setScene(scene);
            primaryStage.setMinHeight(650);
            primaryStage.setMinWidth(900);
            primaryStage.show();

        } catch (IOException e) {
            ErrorLogger.log(e, "Failed to load the main application view (MainLayout.fxml) or resource bundle. Check FXML file path and structure.");
        } catch (Exception e) {
            ErrorLogger.log(e, "An unexpected error occurred during application startup.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
