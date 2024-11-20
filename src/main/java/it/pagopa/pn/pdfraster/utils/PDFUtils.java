package it.pagopa.pn.pdfraster.utils;

import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

@Component
@CustomLog
public class PDFUtils {

    private PDFUtils() {
    }



    public static BufferedImage cropImage(BufferedImage originalImage,float dpi, Integer[] cropbox) {
        float upscale = dpi / 72f;

        // Calcola le nuove dimensioni dell'immagine croppata basate su DPI
        int cropWidth = (int) Math.ceil((cropbox[2] - cropbox[0]) * upscale);
        int cropHeight = (int) Math.ceil((cropbox[3] - cropbox[1]) * upscale);

        // Controlla se altezza e larghezza sono uguali a prima
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        if (originalWidth == cropWidth && originalHeight == cropHeight) {
            return originalImage;
        }

        // Calcola la posizione Y corretta per il crop

        int cropX = (int) (cropbox[0] * upscale);
        int cropY = (int) (originalImage.getHeight() - (cropbox[3] * upscale));
        if (cropY<0){
            cropY=0;
            cropHeight=originalHeight;
        }

        return originalImage.getSubimage(cropX, cropY, cropWidth, cropHeight);
    }

    public static BufferedImage rotateImage(BufferedImage img, double angle) {
        int w = img.getWidth();
        int h = img.getHeight();
        boolean isLandScape = w > h;
        if (!isLandScape) {
            return img;
        }
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((double) (newWidth - w) / 2, (double) (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

}
