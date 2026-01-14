package com.gym.app.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.gym.app.model.Attendance;
import com.gym.app.model.Payment;

public class ReportExporter {

    private static void ensureParentDirectory(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    // --- Excel Export using Apache POI ---

    public static void exportRevenueToExcel(List<Payment> payments, String filePath) throws IOException {
        ensureParentDirectory(filePath);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Revenue Report");

        // Create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Payment ID");
        header.createCell(1).setCellValue("Member ID");
        header.createCell(2).setCellValue("Amount");
        header.createCell(3).setCellValue("Payment Date");
        header.createCell(4).setCellValue("Period (Months)");

        // Populate data rows
        int rowNum = 1;
        for (Payment payment : payments) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(payment.getPaymentId());
            row.createCell(1).setCellValue(payment.getMemberId());
            row.createCell(2).setCellValue(payment.getAmount());
            row.createCell(3).setCellValue(payment.getPaymentDate().toString());
            row.createCell(4).setCellValue(payment.getPeriodMonths());
        }

        // Write the output to a file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
        System.out.println("Revenue report exported to Excel: " + filePath);
    }

    public static void exportAttendanceToExcel(List<Attendance> attendanceList, String filePath) throws IOException {
        ensureParentDirectory(filePath);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");

        // Create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Attendance ID");
        header.createCell(1).setCellValue("Member ID");
        header.createCell(2).setCellValue("Timestamp");

        // Populate data rows
        int rowNum = 1;
        for (Attendance attendance : attendanceList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(attendance.getAttendanceId());
            row.createCell(1).setCellValue(attendance.getMemberId());
            row.createCell(2).setCellValue(attendance.getTimestamp().toString());
        }

        // Write the output to a file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
        System.out.println("Attendance report exported to Excel: " + filePath);
    }

    // --- PDF Export (Placeholder/Simple Text File) ---
    // Note: Implementing a full Java PDF library (like iText or Apache FOP) is complex
    // and requires specific setup. For this project, we will create a simple text file
    // representation as a placeholder for the PDF export. The README will explain
    // the necessary steps for a full PDF implementation.

    public static void exportRevenueToPDF(List<Payment> payments, String filePath) throws IOException {
        ensureParentDirectory(filePath);
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filePath)) {
            writer.println("--- Revenue Report ---");
            writer.println("Generated on: " + java.time.LocalDate.now());
            writer.println("----------------------");
            writer.printf("%-12s %-10s %-10s %-15s %-10s\n", "Payment ID", "Member ID", "Amount", "Payment Date", "Period");
            writer.println("----------------------------------------------------------------");
            for (Payment payment : payments) {
                writer.printf("%-12d %-10d %-10.2f %-15s %-10d\n",
                        payment.getPaymentId(),
                        payment.getMemberId(),
                        payment.getAmount(),
                        payment.getPaymentDate().toString(),
                        payment.getPeriodMonths());
            }
            writer.println("----------------------------------------------------------------");
        }
        System.out.println("Revenue report exported to (simulated) PDF: " + filePath);
    }

    public static void exportAttendanceToPDF(List<Attendance> attendanceList, String filePath) throws IOException {
        ensureParentDirectory(filePath);
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filePath)) {
            writer.println("--- Attendance Report ---");
            writer.println("Generated on: " + java.time.LocalDate.now());
            writer.println("-------------------------");
            writer.printf("%-15s %-10s %-25s\n", "Attendance ID", "Member ID", "Timestamp");
            writer.println("----------------------------------------------------------------");
            for (Attendance attendance : attendanceList) {
                writer.printf("%-15d %-10d %-25s\n",
                        attendance.getAttendanceId(),
                        attendance.getMemberId(),
                        attendance.getTimestamp().toString());
            }
            writer.println("----------------------------------------------------------------");
        }
        System.out.println("Attendance report exported to (simulated) PDF: " + filePath);
    }
}
