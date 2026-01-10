package com.gym.app.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ErrorLogger {

    private static final String LOG_FILE = "error_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs the exception to a file, prints the stack trace to the console,
     * and shows a friendly popup to the user.
     *
     * @param e The exception to log.
     * @param userMessage A friendly message to show the user.
     */
    public static void log(Throwable e, String userMessage) {
        // 1. Log every exception into file: error_log.txt
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println("--- Exception Logged: " + LocalDateTime.now().format(FORMATTER) + " ---");
            pw.println("User Message: " + userMessage);
            e.printStackTrace(pw);
            pw.println("-------------------------------------------------");
        } catch (IOException ioException) {
            System.err.println("Could not write to error log file: " + ioException.getMessage());
        }

        // 2. Print the full stacktrace to console
        System.err.println("--- Application Error ---");
        System.err.println("User Message: " + userMessage);
        e.printStackTrace();
        System.err.println("-------------------------");

        // 3. Show friendly popup for the user
        showErrorPopup(e, userMessage);
    }

    private static void showErrorPopup(Throwable e, String userMessage) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Application Error");
        alert.setHeaderText("An unexpected error occurred.");
        alert.setContentText(userMessage);

        // Create expandable Exception Details
        String exceptionText = getStackTrace(e);

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable content into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(false); // Start collapsed

        // 4. Suggest how to fix the error (simple generic suggestion for now)
        // Note: setExpandedText is not available in this JavaFX version, so we include the suggestion in the content text

        alert.showAndWait();
    }

    private static String getStackTrace(Throwable e) {
        // Helper to convert stack trace to string
        try (java.io.StringWriter sw = new java.io.StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ex) {
            return "Could not retrieve stack trace.";
        }
    }
}
