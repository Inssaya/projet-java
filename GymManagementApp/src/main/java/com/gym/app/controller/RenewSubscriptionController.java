package com.gym.app.controller;

import com.gym.app.model.Member;
import com.gym.app.model.Payment;
import com.gym.app.service.MemberService;
import com.gym.app.service.PaymentService;
import com.gym.app.util.ErrorLogger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class RenewSubscriptionController implements Initializable {

    @FXML
    private Label memberNameLabel;
    @FXML
    private Label currentExpiryLabel;
    @FXML
    private Label newExpiryLabel;
    @FXML
    private TextField amountField;
    @FXML
    private TextField customMonthsField;
    @FXML
    private ToggleGroup periodGroup;
    @FXML
    private RadioButton radio1Month;
    @FXML
    private RadioButton radio2Months;
    @FXML
    private RadioButton radio3Months;

    private Member member;
    private Stage dialogStage;
    private MemberService memberService;
    private PaymentService paymentService;
    
    // Initialize services in the constructor or initialize method
    public RenewSubscriptionController() {
        this.memberService = new MemberService();
        this.paymentService = new PaymentService();
    }
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Listener for radio buttons
        periodGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                customMonthsField.setText(""); // Clear custom field when a radio button is selected
                calculateNewExpiry();
            }
        });

        // Listener for custom months field
        customMonthsField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                periodGroup.selectToggle(null); // Deselect radio buttons
            }
            calculateNewExpiry();
        });
        
        // Force the amount field to be numeric
        amountField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*([\\.]\\d{0,2})?")) {
                    amountField.setText(oldValue);
                }
            }
        });
        
        // Default selection
        radio1Month.setSelected(true);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setMember(Member member) {
        this.member = member;
        if (member != null) {
            memberNameLabel.setText(member.getFullName());
            currentExpiryLabel.setText(member.getExpiryDate().format(dateFormatter));
            calculateNewExpiry();
        }
    }

    private int getSelectedMonths() {
        if (periodGroup.getSelectedToggle() != null) {
            RadioButton selected = (RadioButton) periodGroup.getSelectedToggle();
            if (selected == radio1Month) return 1;
            if (selected == radio2Months) return 2;
            if (selected == radio3Months) return 3;
        }
        
        try {
            String customText = customMonthsField.getText();
            if (!customText.isEmpty()) {
                return Integer.parseInt(customText);
            }
        } catch (NumberFormatException e) {
            // Ignore, will be caught in validation
        }
        return 0;
    }

    private void calculateNewExpiry() {
        if (member == null) return;

        int months = getSelectedMonths();
        if (months > 0) {
            LocalDate currentExpiry = member.getExpiryDate();
            // If the current expiry is in the past, start the renewal from today.
            LocalDate baseDate = currentExpiry.isBefore(LocalDate.now()) ? LocalDate.now() : currentExpiry;
            LocalDate newExpiry = baseDate.plusMonths(months);
            newExpiryLabel.setText(newExpiry.format(dateFormatter));
        } else {
            newExpiryLabel.setText("[Select Period]");
        }
    }

    @FXML
    private void handleRenew() {
        int months = getSelectedMonths();
        double amount;

        // 1. Validation
        if (months <= 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a valid renewal period.");
            return;
        }
        try {
            amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Amount must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid payment amount.");
            return;
        }

        // 2. Update Expiry Date
        LocalDate newExpiry = memberService.renewSubscription(member.getMemberId(), months);

        if (newExpiry != null) {
            // 3. Add Payment Record
            Payment payment = new Payment(member.getMemberId(), amount, LocalDate.now(), months);
            paymentService.addPayment(payment);

            // 4. Update member object in memory
            member.setExpiryDate(newExpiry);
            
            // 5. Play short beep (Placeholder)
            System.out.println("ACTION: Play short beep sound.");

            // 6. Send WhatsApp renewal message (Placeholder)
            System.out.println("ACTION: Send WhatsApp renewal message to " + member.getPhone());

            // 7. Show success message
            showAlert(Alert.AlertType.INFORMATION, "Renewal Successful", 
                      String.format("Subscription renewed for %s months. New expiry date: %s", months, newExpiry.format(dateFormatter)));

            // 8. Close the dialog
            dialogStage.close();
        } else {
            showAlert(Alert.AlertType.ERROR, "Renewal Failed", "Could not process subscription renewal. Check error log.");
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
