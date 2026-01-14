package com.gym.app.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class QRGenerator {

    /**
     * Generates a QR code image as a JavaFX Image object.
     * @param text The data to encode in the QR code.
     * @param width The width of the QR code image.
     * @param height The height of the QR code image.
     * @return A JavaFX Image object.
     * @throws WriterException if an error occurs during QR code generation.
     */
    public static Image generateQRImage(String text, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /**
     * Generates a QR code image and saves it to a file.
     * @param text The data to encode in the QR code.
     * @param width The width of the QR code image.
     * @param height The height of the QR code image.
     * @param filePath The path to save the PNG file.
     * @throws WriterException if an error occurs during QR code generation.
     * @throws IOException if an error occurs during file writing.
     */
    public static void saveQRImage(String text, int width, int height, String filePath) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file.toPath());
    }
}
