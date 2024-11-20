package it.pagopa.pn.pdfraster.utils;

import it.pagopa.pn.pdfraster.configuration.aws.PdfTransformationConfiguration;
import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static it.pagopa.pn.pdfraster.utils.TestUtils.getTestImageFromResources;

@SpringBootTestWebEnv
@CustomLog
class PDFUtilsTest {
    private static Integer[] cropbox;
    private static BufferedImage image;
    private static float dpi;

    @BeforeAll
    public static void init(@Autowired PdfTransformationConfiguration config) throws IOException {
        cropbox = Arrays.stream(config.getPdfTransformationConfigParams().getCropbox().split(",")).map(a -> Integer.parseInt(a.trim())).toArray(Integer[]::new);
        dpi = config.getPdfTransformationConfigParams().getDpi().floatValue();
        byte[] imageData = getTestImageFromResources();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);
        image = ImageIO.read(byteArrayInputStream);
    }

    @Test
    void cropImageOk() {
        BufferedImage newImage = PDFUtils.cropImage(image, dpi, cropbox);
        Assertions.assertNotNull(newImage);
    }

    @Test
    void rotateImageOk() {
        BufferedImage newImage = PDFUtils.rotateImage(image, 90);
        Assertions.assertNotNull(newImage);
        Assertions.assertEquals(image.getHeight(), newImage.getWidth());
        Assertions.assertEquals(image.getWidth(), newImage.getHeight());
    }

}
