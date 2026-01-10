package com.gym.app.controller;

import com.gym.app.model.Member;
import com.gym.app.service.MemberService;
import com.gym.app.util.ErrorLogger;
import com.gym.app.util.QRGenerator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.UUID;

public class MemberFormController implements Initializable {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker joinDatePicker;
    @FXML
    private DatePicker expiryDatePicker;
    @FXML
    private TextField qrValueField;
    @FXML
    private TextArea notesArea;
    @FXML
    private ImageView photoView;
    @FXML
    private ImageView qrCodeView;

    private MemberService memberService = new MemberService();
    private Member member;
    private Stage dialogStage;
    private boolean isNewMember = true;
    private String currentPhotoPath;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set default dates for a new member
        joinDatePicker.setValue(LocalDate.now());
        expiryDatePicker.setValue(LocalDate.now().plusMonths(1));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the member to be edited. If null, it's a new member form.
     */
    public void setMember(Member member) {
        this.member = member;
        this.isNewMember = (member == null);

        if (!isNewMember) {
            titleLabel.setText("Edit Member: " + member.getFullName());
            fullNameField.setText(member.getFullName());
            phoneField.setText(member.getPhone());
            joinDatePicker.setValue(member.getJoinDate());
            expiryDatePicker.setValue(member.getExpiryDate());
            qrValueField.setText(member.getQrCodeValue());
            notesArea.setText(member.getNotes());
            currentPhotoPath = member.getPhotoPath();
            
            // Load photo and QR code
            loadPhoto(currentPhotoPath);
            loadQrCode(member.getQrCodeValue());
        } else {
            titleLabel.setText("Add New Member");
        }
    }

    private void loadPhoto(String path) {
        if (path != null && !path.isEmpty()) {
            try {
                Image image = new Image(new File(path).toURI().toString());
                photoView.setImage(image);
            } catch (Exception e) {
                ErrorLogger.log(e, "Failed to load member photo from path: " + path);
                // Optionally set a placeholder image
            }
        }
    }
    
    private void loadQrCode(String qrValue) {
        if (qrValue != null && !qrValue.isEmpty()) {
            try {
                Image qrImage = QRGenerator.generateQRImage(qrValue, 150, 150);
                qrCodeView.setImage(qrImage);
            } catch (Exception e) {
                ErrorLogger.log(e, "Failed to generate/load QR code for value: " + qrValue);
                // Optionally set a placeholder image
            }
        }
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Member Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(dialogStage);

        if (selectedFile != null) {
            // In a real application, you would copy this file to a dedicated 'photos' directory
            // and save the relative path. For simplicity, we'll save the absolute path for now.
            currentPhotoPath = selectedFile.getAbsolutePath();
            loadPhoto(currentPhotoPath);
        }
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            try {
                if (isNewMember) {
                    // 1. Generate UNIQUE QR ID
                    String qrCodeValue = UUID.randomUUID().toString();
                    
                    // 2. Create new Member object
                    Member newMember = new Member(
                        fullNameField.getText(),
                        phoneField.getText(),
                        joinDatePicker.getValue(),
                        expiryDatePicker.getValue(),
                        qrCodeValue,
                        currentPhotoPath,
                        notesArea.getText()
                    );
                    
                    // 3. Save to DB
                    member = memberService.addMember(newMember);
                    
                    if (member != null) {
                        // 4. Generate visible QR code PNG (and save it to a file for printing/sharing)
                        // The QR code image is generated on the fly in loadQrCode, but we need to save the PNG file.
                        // We'll use a utility method for this.
                        String qrFilePath = "qrcodes/" + member.getMemberId() + ".png";
                        try {
                            QRGenerator.saveQRImage(member.getQrCodeValue(), 300, 300, qrFilePath);
                        } catch (Exception e) {
                            ErrorLogger.log(e, "Failed to save QR code image to file: " + qrFilePath);
                        }
                        
                        // Update the form to show the generated QR code and value
                        qrValueField.setText(member.getQrCodeValue());
                        loadQrCode(member.getQrCodeValue());
                        
                        // Show success message
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle(dialogStage.getTitle());
                        alert.setHeaderText(null);
                        alert.setContentText("Member " + member.getFullName() + " added successfully. QR Code saved to " + qrFilePath);
                        alert.showAndWait();
                        
                        // Close the dialog
                        dialogStage.close();
                    }
                    
                } else {
                    // Update existing member
                    member.setFullName(fullNameField.getText());
                    member.setPhone(phoneField.getText());
                    member.setJoinDate(joinDatePicker.getValue());
                    member.setExpiryDate(expiryDatePicker.getValue());
                    member.setPhotoPath(currentPhotoPath);
                    member.setNotes(notesArea.getText());

                    if (memberService.updateMember(member)) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle(dialogStage.getTitle());
                        alert.setHeaderText(null);
                        alert.setContentText("Member " + member.getFullName() + " updated successfully.");
                        alert.showAndWait();
                        dialogStage.close();
                    }
                }
            } catch (Exception e) {
                ErrorLogger.log(e, "Error saving member data.");
            }
        }
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (fullNameField.getText() == null || fullNameField.getText().length() == 0) {
            errorMessage += "No valid full name!\n";
        }
        if (joinDatePicker.getValue() == null) {
            errorMessage += "No valid join date!\n";
        }
        if (expiryDatePicker.getValue() == null) {
            errorMessage += "No valid expiry date!\n";
        } else if (expiryDatePicker.getValue().isBefore(joinDatePicker.getValue())) {
            errorMessage += "Expiry date cannot be before join date!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    @FXML
    private void handlePrintQR() {
        if (qrCodeView.getImage() != null) {
            // Create a new stage for printing
            Stage printStage = new Stage();
            
            // Create a pane to hold the QR code image
            ImageView qrImageView = new ImageView(qrCodeView.getImage());
            qrImageView.setPreserveRatio(true);
            qrImageView.setFitWidth(300); // Set appropriate size for printing
            
            VBox root = new VBox(10);
            root.setAlignment(javafx.geometry.Pos.CENTER);
            root.getChildren().add(qrImageView);
            
            // Add a close button
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> printStage.close());
            root.getChildren().add(closeButton);
            
            Scene scene = new Scene(root, 400, 400);
            printStage.setTitle("QR Code - " + fullNameField.getText());
            printStage.setScene(scene);
            printStage.show();
            
            // Inform the user
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "QR code displayed in a new window. You can print from the browser or save the image.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No QR code available to print.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleShareQR() {
        if (member != null && member.getQrCodeValue() != null && !member.getQrCodeValue().isEmpty()) {
            // Create a message with the member's information and QR code
            String message = "Hello! Here is your gym membership information:\n" +
                           "Name: " + member.getFullName() + "\n" +
                           "QR Code: " + member.getQrCodeValue() + "\n" +
                           "Expiry Date: " + member.getExpiryDate() + "\n" +
                           "Thank you for being a member!";
            
            // Encode the message for URL
            String encodedMessage = java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8);
            
            // Create WhatsApp URL
            String whatsappUrl = "https://wa.me/" + member.getPhone() + "?text=" + encodedMessage;
            
            // Open WhatsApp Web in the default browser
            try {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(whatsappUrl));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "WhatsApp message prepared. Please send it from your browser.");
                alert.showAndWait();
            } catch (Exception e) {
                ErrorLogger.log(e, "Failed to open WhatsApp.");
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open WhatsApp. Please make sure you have internet connection.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Member information not available for sharing.");
            alert.showAndWait();
        }
    }
}
