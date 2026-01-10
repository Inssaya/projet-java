package com.gym.app.controller;

import com.gym.app.model.Member;
import com.gym.app.service.MemberService;
import com.gym.app.util.ErrorLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.stage.Stage;
import javafx.scene.Scene;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.util.List;
public class MemberManagementController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Member> memberTable;
    @FXML
    private TableColumn<Member, Integer> idColumn;
    @FXML
    private TableColumn<Member, String> nameColumn;
    @FXML
    private TableColumn<Member, String> phoneColumn;
    @FXML
    private TableColumn<Member, LocalDate> joinDateColumn;
    @FXML
    private TableColumn<Member, LocalDate> expiryDateColumn;
    @FXML
    private TableColumn<Member, String> statusColumn;
    @FXML
    private TableColumn<Member, String> qrValueColumn;

    private MemberService memberService = new MemberService();
    private ObservableList<Member> memberList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        joinDateColumn.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        qrValueColumn.setCellValueFactory(new PropertyValueFactory<>("qrCodeValue"));
        
        // Custom cell factory for status column to apply CSS styles (will be implemented later)
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Load initial data
        loadMembers("");
    }

    private void loadMembers(String searchTerm) {
        try {
            // The searchMembers method in MemberService will handle the actual search.
            List<Member> members = memberService.searchMembers(searchTerm);
            memberList.setAll(members);
            memberTable.setItems(memberList);
        } catch (Exception e) {
            ErrorLogger.log(e, "Failed to load members into the table.");
        }
    }

    private void openMemberForm(Member member) {
        try {
            // Get the resource bundle from the current controller's resource bundle
            ResourceBundle resources = memberTable.getScene().getWindow().getScene().getProperties().containsKey("resources") 
                                     ? (ResourceBundle) memberTable.getScene().getWindow().getScene().getProperties().get("resources")
                                     : null;
            
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gym/app/view/MemberForm.fxml"), resources);
            Parent root = loader.load();

            // Create the dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources != null ? resources.getString(member == null ? "member.add" : "member.edit") : (member == null ? "Add New Member" : "Edit Member"));
            dialogStage.initOwner(memberTable.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Set the controller's stage and member
            MemberFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMember(member);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            // Refresh the table after the dialog is closed
            loadMembers(searchField.getText());

        } catch (IOException e) {
            ErrorLogger.log(e, "Failed to open Member Form dialog.");
        }
    }

    private void openRenewSubscriptionDialog(Member member) {
        try {
            // Get the resource bundle from the current controller's resource bundle
            ResourceBundle resources = memberTable.getScene().getWindow().getScene().getProperties().containsKey("resources") 
                                     ? (ResourceBundle) memberTable.getScene().getWindow().getScene().getProperties().get("resources")
                                     : null;
            
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gym/app/view/RenewSubscription.fxml"), resources);
            Parent root = loader.load();

            // Create the dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources != null ? resources.getString("payment.title") : "Renew Subscription");
            dialogStage.initOwner(memberTable.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Set the controller's stage and member
            RenewSubscriptionController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMember(member);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            // Refresh the table after the dialog is closed
            loadMembers(searchField.getText());

        } catch (IOException e) {
            ErrorLogger.log(e, "Failed to open Renew Subscription dialog.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText();
        loadMembers(searchTerm);
    }

    @FXML
    private void handleAddMember() {
        openMemberForm(null);
    }

    @FXML
    private void handleEditMember() {
        Member selectedMember = memberTable.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            openMemberForm(selectedMember);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a member in the table.");
        }
    }

    @FXML
    private void handleRenewSubscription() {
        Member selectedMember = memberTable.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            openRenewSubscriptionDialog(selectedMember);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a member in the table.");
        }
    }
}
