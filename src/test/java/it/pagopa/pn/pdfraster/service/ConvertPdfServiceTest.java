package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

import static it.pagopa.pn.pdfraster.utils.TestUtils.getFileJpeg2000FromResources;

@ActiveProfiles("test")
@SpringBootTestWebEnv
class ConvertPdfServiceTest {

    private static final int DARK_LUMINANCE_THRESHOLD = 128;
    private static final double MIN_DARK_PIXELS_RATIO = 0.5;
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    private ConvertPdfService convertPdfService;

    @Test
    void jpeg2000Reader_registered_isAvailable() {
        Assertions.assertTrue(ImageIO.getImageReadersByFormatName("JPEG2000").hasNext(),
                "Reader JPEG2000 non registrato");
    }

    @Test
    void convertPdfToImage_jpeg2000Image_keepsDarkPixels() throws IOException {
        ByteArrayOutputStream result = convertPdfService.convertPdfToImage(getFileJpeg2000FromResources()).block(BLOCK_TIMEOUT);

        Assertions.assertNotNull(result);
        try (PDDocument document = Loader.loadPDF(result.toByteArray())) {
            Assertions.assertEquals(1, document.getNumberOfPages());
            PDImageXObject imageXObject = getSingleImage(document);
            BufferedImage image = imageXObject.getImage();

            long darkPixels = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    int luminance = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
                    if (luminance < DARK_LUMINANCE_THRESHOLD) {
                        darkPixels++;
                    }
                }
            }
            double ratio = (double) darkPixels / ((long) image.getWidth() * image.getHeight());
            Assertions.assertTrue(ratio >= MIN_DARK_PIXELS_RATIO,
                    "Percentuale pixel scuri insufficiente: " + ratio);
        }
    }

    private static PDImageXObject getSingleImage(PDDocument document) throws IOException {
        PDImageXObject found = null;
        PDResources resources = document.getPage(0).getResources();
        for (COSName name : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(name);
            if (xObject instanceof PDImageXObject) {
                Assertions.assertNull(found, "Trovata piu' di una immagine");
                found = (PDImageXObject) xObject;
            }
        }
        Assertions.assertNotNull(found, "Nessuna immagine trovata");
        return found;
    }
}
