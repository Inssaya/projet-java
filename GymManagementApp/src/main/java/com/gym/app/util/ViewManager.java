package com.gym.app.util;

import com.gym.app.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import com.gym.app.controller.MainController;
import java.io.IOException;
import java.util.ResourceBundle;

public class ViewManager {

    private static Pane mainContentPane;
    private static MainController mainController;
    private static ResourceBundle resourceBundle;

    public static void setMainContentPane(Pane pane) {
        mainContentPane = pane;
    }

    public static void setResourceBundle(ResourceBundle bundle) {
        resourceBundle = bundle;
    }

    public static void setMainController(MainController controller) {
        mainController = controller;
    }

    /**
     * Loads an FXML view and sets it as the content of the main content pane.
     * @param fxmlFileName The name of the FXML file (e.g., "MemberManagement.fxml").
     */
    public static void loadView(String fxmlFileName) {
        if (mainContentPane == null) {
            ErrorLogger.log(new IllegalStateException("Main content pane not set."), "Cannot load view. Main content pane is null.");
            return;
        }

        try {
            // Get the controller of the currently displayed view for cleanup
            Object oldController = null;
            if (!mainContentPane.getChildren().isEmpty()) {
                Parent oldView = (Parent) mainContentPane.getChildren().get(0);
                oldController = oldView.getProperties().get("controller");
            }
            
            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/gym/app/view/" + fxmlFileName), resourceBundle);
            Parent view = loader.load();
            
            // Store the new controller for later cleanup
            view.getProperties().put("controller", loader.getController());

            // Clear existing content and add the new view
            mainContentPane.getChildren().clear();
            mainContentPane.getChildren().add(view);
            
            // Perform cleanup on the old controller
            if (oldController != null && mainController != null) {
                mainController.cleanupController(oldController);
            }
            
            // Ensure the loaded view fills the parent pane
            if (view instanceof Pane) {
                ((Pane) view).prefWidthProperty().bind(mainContentPane.widthProperty());
                ((Pane) view).prefHeightProperty().bind(mainContentPane.heightProperty());
            }

        } catch (IOException e) {
            ErrorLogger.log(e, "Failed to load FXML view: " + fxmlFileName);
        }
    }
}
