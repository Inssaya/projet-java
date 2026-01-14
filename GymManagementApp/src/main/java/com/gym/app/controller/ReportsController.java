package com.gym.app.controller;

import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ResourceBundle;

import com.gym.app.model.Attendance;
import com.gym.app.model.Payment;
import com.gym.app.service.AttendanceService;
import com.gym.app.service.PaymentService;
import com.gym.app.util.AppPaths;
import com.gym.app.util.ErrorLogger;
import com.gym.app.util.ReportExporter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ReportsController implements Initializable {

    @FXML
    private Label monthlyRevenueLabel;
    @FXML
    private Label annualRevenueLabel;
    @FXML
    private TableView<Payment> paymentTable;
    @FXML
    private TableColumn<Payment, LocalDate> payDateColumn;
    @FXML
    private TableColumn<Payment, Integer> payMemberIdColumn;
    @FXML
    private TableColumn<Payment, Double> payAmountColumn;
    @FXML
    private TableColumn<Payment, Integer> payPeriodColumn;
    @FXML
    private TableView<Attendance> attendanceTable;
    @FXML
    private TableColumn<Attendance, LocalDateTime> attTimestampColumn;
    @FXML
    private TableColumn<Attendance, Integer> attMemberIdColumn;

    private PaymentService paymentService = new PaymentService();
    private AttendanceService attendanceService = new AttendanceService();
    private ObservableList<Payment> paymentList = FXCollections.observableArrayList();
    private ObservableList<Attendance> attendanceList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize Payment Table
        payDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        payMemberIdColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        payAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        payPeriodColumn.setCellValueFactory(new PropertyValueFactory<>("periodMonths"));
        paymentTable.setItems(paymentList);

        // Initialize Attendance Table
        attTimestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        attMemberIdColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        attendanceTable.setItems(attendanceList);

        // Load Data
        loadRevenueData();
        loadAttendanceData();
    }

    private void loadRevenueData() {
        try {
            // Monthly Revenue
            YearMonth currentMonth = YearMonth.now();
            LocalDate startOfMonth = currentMonth.atDay(1);
            LocalDate endOfMonth = currentMonth.atEndOfMonth();
            double monthlyRevenue = paymentService.getTotalRevenue(startOfMonth, endOfMonth);
            monthlyRevenueLabel.setText(String.format("$%.2f", monthlyRevenue));

            // Annual Revenue
            LocalDate startOfYear = LocalDate.now().with(java.time.temporal.TemporalAdjusters.firstDayOfYear());
            LocalDate endOfYear = LocalDate.now().with(java.time.temporal.TemporalAdjusters.lastDayOfYear());
            double annualRevenue = paymentService.getTotalRevenue(startOfYear, endOfYear);
            annualRevenueLabel.setText(String.format("$%.2f", annualRevenue));

            // Load all payments
            paymentList.setAll(paymentService.getAllPayments());
            
        } catch (Exception e) {
            ErrorLogger.log(e, "Failed to load revenue data.");
        }
    }
    
    private void loadAttendanceData() {
        try {
            attendanceList.setAll(attendanceService.getAllAttendance());
        } catch (Exception e) {
            ErrorLogger.log(e, "Failed to load attendance data.");
        }
    }

    @FXML
    private void handleExportRevenuePDF() {
        try {
            Path filePath = AppPaths.getReportsDir().resolve("RevenueReport.pdf");
            ReportExporter.exportRevenueToPDF(paymentList, filePath.toString());
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Revenue report exported to:\n" + filePath + "\n(simulated PDF)");
        } catch (Exception e) {
            ErrorLogger.log(e, "Failed to export revenue report to PDF.");
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export revenue report to PDF. Check error log.");
        }
    }

    @FXML
    private void handleExportRevenueExcel() {
        try {
            Path filePath = AppPaths.getReportsDir().resolve("RevenueReport.xlsx");
            ReportExporter.exportRevenueToExcel(paymentList, filePath.toString());
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Revenue report exported to:\n" + filePath);
        } catch (Exception e) {
            ErrorLogger.log(e, "Failed to export revenue report to Excel.");
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export revenue report to Excel. Check error log.");
        }
    }

    @FXML
    private void handleExportAttendancePDF() {
        try {
            Path filePath = AppPaths.getReportsDir().resolve("AttendanceReport.pdf");
            ReportExporter.exportAttendanceToPDF(attendanceList, filePath.toString());
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Attendance report exported to:\n" + filePath + "\n(simulated PDF)");
        } catch (Exception e) {
            ErrorLogger.log(e, "Failed to export attendance report to PDF.");
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export attendance report to PDF. Check error log.");
        }
    }

    @FXML
    private void handleExportAttendanceExcel() {
        try {
            Path filePath = AppPaths.getReportsDir().resolve("AttendanceReport.xlsx");
            ReportExporter.exportAttendanceToExcel(attendanceList, filePath.toString());
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Attendance report exported to:\n" + filePath);
        } catch (Exception e) {
            ErrorLogger.log(e, "Failed to export attendance report to Excel.");
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export attendance report to Excel. Check error log.");
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
