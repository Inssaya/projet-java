package com.gym.app.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.gym.app.model.Member;
import com.gym.app.service.AttendanceService;
import com.gym.app.service.MemberService;
import com.gym.app.util.ErrorLogger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;

public class CheckInController implements Initializable {

    @FXML
    private ImageView cameraFeedView;
    @FXML
    private ImageView memberPhotoView;
    @FXML
    private Label memberNameLabel;
    @FXML
    private Label memberStatusLabel;
    @FXML
    private Label memberExpiryLabel;
    @FXML
    private Label memberPhoneLabel;

    private FrameGrabber grabber = null;
    private ScheduledExecutorService timer;
    private OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
    private MemberService memberService = new MemberService();
    private AttendanceService attendanceService = new AttendanceService();
    private MultiFormatReader qrReader = new MultiFormatReader();
    private long lastCheckInTime = 0;
    private final long CHECK_IN_COOLDOWN_MS = 5000; // 5 seconds cooldown

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set initial placeholder text
        updateStatusDisplay(null, "Awaiting Scan", "status-expiring");
    }

    @FXML
    private void handleStartCamera() {
        if (grabber != null) {
            stopCamera();
            return;
        }

        try {
            // Use default camera (index 0)
            grabber = new VideoInputFrameGrabber(0);
            grabber.start();

            // Start a scheduled executor to grab frames periodically
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(this::grabAndProcessFrame, 0, 33, TimeUnit.MILLISECONDS); // ~30 FPS

        } catch (FrameGrabber.Exception e) {
            ErrorLogger.log(e, "Failed to start camera. Check if camera is connected and drivers are installed (JavaCV dependency issue)." +
                    " Error details: " + e.getMessage());
            updateStatusDisplay(null, "Camera Error", "status-expired");
            
            // Try alternative camera index
            try {
                grabber = new VideoInputFrameGrabber(1);
                grabber.start();
                
                timer = Executors.newSingleThreadScheduledExecutor();
                timer.scheduleAtFixedRate(this::grabAndProcessFrame, 0, 33, TimeUnit.MILLISECONDS);
                
                updateStatusDisplay(null, "Camera Started (Alternative)", "status-active");
            } catch (FrameGrabber.Exception ex) {
                ErrorLogger.log(ex, "Failed to start camera on alternative index as well.");
                updateStatusDisplay(null, "Camera Error", "status-expired");
            }
        }
    }

    private void grabAndProcessFrame() {
        try {
            Frame frame = grabber.grab();
            if (frame != null) {
                Mat mat = converter.convert(frame);
                
                // Convert to grayscale for better QR detection
                Mat grayMat = new Mat();
                opencv_imgproc.cvtColor(mat, grayMat, opencv_imgproc.COLOR_BGR2GRAY);

                // Convert Mat to BufferedImage
                BufferedImage image = matToBufferedImage(mat);

                // Decode QR Code
                String qrCodeValue = decodeQRCode(image);

                // Update camera feed on UI thread
                Platform.runLater(() -> cameraFeedView.setImage(matToImage(mat)));

                if (qrCodeValue != null) {
                    processCheckIn(qrCodeValue);
                }
            }
        } catch (FrameGrabber.Exception e) {
            ErrorLogger.log(e, "Error grabbing frame from camera.");
            stopCamera();
        }
    }

    private String decodeQRCode(BufferedImage bufferedImage) {
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
            Result result = qrReader.decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            // QR code not found in this frame, this is normal
            return null;
        } catch (Exception e) {
            ErrorLogger.log(e, "Error decoding QR code.");
            return null;
        }
    }

    private void processCheckIn(String qrCodeValue) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckInTime < CHECK_IN_COOLDOWN_MS) {
            // Ignore scan due to cooldown
            return;
        }
        lastCheckInTime = currentTime;

        Member member = memberService.getMemberByQrCodeValue(qrCodeValue);

        if (member != null) {
            String status = member.getStatus();
            
            if ("Active".equals(status) || "Expiring".equals(status)) {
                // IF membership Active
                // Play short pip pip (Placeholder)
                System.out.println("ACTION: Play short pip pip sound.");
                
                // Log attendance
                attendanceService.logAttendance(member.getMemberId());
                
                // Show popup with details
                long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), member.getExpiryDate());
                String statusClassFinal = "Expiring".equals(status) ? "status-expiring" : "status-active";
                
                Platform.runLater(() -> {
                    updateStatusDisplay(member, "Welcome, " + member.getFullName() + "!", statusClassFinal);
                    memberExpiryLabel.setText("Expiry: " + member.getExpiryDate() + " (" + daysRemaining + " days remaining)");
                });

            } else if ("Expired".equals(status)) {
                // IF membership Expired
                // Play long piiiip piiip (Placeholder)
                System.out.println("ACTION: Play long piiiip piiip sound.");
                
                // Show red alert
                Platform.runLater(() -> {
                    updateStatusDisplay(member, "Membership Expired!", "status-expired");
                    // Suggest renewal (via a separate dialog or a button on the UI)
                    // For now, the UI update is the alert.
                });
            }
        } else {
            // QR code not recognized
            Platform.runLater(() -> {
                updateStatusDisplay(null, "QR Code Not Recognized", "status-expired");
            });
        }
    }

    private void updateStatusDisplay(Member member, String statusText, String statusClass) {
        if (member != null) {
            memberNameLabel.setText(member.getFullName());
            memberPhoneLabel.setText("Phone: " + member.getPhone());
            
            // Load photo
            if (member.getPhotoPath() != null && !member.getPhotoPath().isEmpty()) {
                try {
                    Image image = new Image(new File(member.getPhotoPath()).toURI().toString());
                    memberPhotoView.setImage(image);
                } catch (Exception e) {
                    memberPhotoView.setImage(null); // Clear image on error
                }
            } else {
                memberPhotoView.setImage(null);
            }
        } else {
            memberNameLabel.setText("Scan QR Code");
            memberPhoneLabel.setText("Phone: N/A");
            memberExpiryLabel.setText("Expiry: N/A");
            memberPhotoView.setImage(null);
        }
        
        memberStatusLabel.setText(statusText);
        memberStatusLabel.getStyleClass().removeAll("status-active", "status-expiring", "status-expired");
        memberStatusLabel.getStyleClass().add(statusClass);
    }

    public void stopCamera() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdownNow();
        }
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (FrameGrabber.Exception e) {
                ErrorLogger.log(e, "Error stopping camera grabber.");
            }
        }
        grabber = null;
        Platform.runLater(() -> cameraFeedView.setImage(null));
    }

    // Utility to convert Mat to JavaFX Image
    private Image matToImage(Mat mat) {
        try {
            BufferedImage bufferedImage = matToBufferedImage(mat);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", os);
            return new Image(new ByteArrayInputStream(os.toByteArray()));
        } catch (IOException e) {
            ErrorLogger.log(e, "Error converting Mat to Image.");
            return null;
        }
    }
    
    // Utility to convert Mat to BufferedImage (required for ZXing)
    private BufferedImage matToBufferedImage(Mat mat) {
        try {
            Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
            Frame frame = converter.convert(mat);
            return java2dConverter.convert(frame);
        } catch (Exception e) {
            ErrorLogger.log(e, "Error converting Mat to BufferedImage");
            return null;
        }
    }
    
    // Ensure camera is stopped when controller is destroyed (e.g., view switched)
    public void shutdown() {
        stopCamera();
    }
}
