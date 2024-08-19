package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.service.ConvertPdfService;
import lombok.CustomLog;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@CustomLog
@Service
@Command(name = "pdftoimage", header = "Converts a PDF document to image(s)", /*versionProvider = Version.class,*/ mixinStandardHelpOptions = true)
public class ConvertPdfServiceImpl implements ConvertPdfService {

    @Option(names = "-password", description = "the password to decrypt the document", arity = "0..1", interactive = true)
    private String password;

    @Option(names = {"-format"}, description = "the image file format (default: ${DEFAULT-VALUE})")
    private String imageFormat = "jpg";

    @Option(names = {"-prefix", "-outputPrefix"}, description = "the filename prefix for image files")
    private String outputPrefix;

    @Option(names = "-page", description = "the only page to extract (1-based)")
    private int page = -1;

    @Option(names = "-startPage", description = "the first page to start extraction (1-based)")
    private int startPage = 1;

    @Option(names = "-endPage", description = "the last page to extract (inclusive)")
    private int endPage = Integer.MAX_VALUE;

    @Option(names = "-color", description = "the color depth (valid: ${COMPLETION-CANDIDATES}) (default: ${DEFAULT-VALUE})")
    private ImageType imageType = ImageType.ARGB;

    @Option(names = {"-dpi", "-resolution"}, description = "the DPI of the output image, default: screen resolution or 96 if unknown")
    private int dpi;

    @Option(names = "-quality", description = "the quality to be used when compressing the image (0 <= quality <= 1) " +
            "(default: 0 for PNG and 1 for the other formats)")
    private float quality = -1;

    @Option(names = "-margin", description = "the output document margin to use on all sides")
    private int iMargin = 0;

    @Option(names = "-cropbox", arity = "4", description = "the page area to export")
    private int[] cropbox;

    @Option(names = "-time", description = "print timing information to stdout")
    private boolean showTime;

    @Option(names = "-subsampling", description = "activate subsampling (for PDFs with huge images)")
    private boolean subsampling;


    @Override
    public ByteArrayOutputStream convertPdfToImage(byte[] file) {

        List<String> writerFormatNames = Arrays.asList(ImageIO.getWriterFormatNames());
        if (!writerFormatNames.contains(imageFormat)) {
            String wfn = String.join(", ", writerFormatNames);
            log.error("Error: Invalid image format {} - supported formats: {}" ,imageFormat, wfn);
        }

        if (quality < 0) {
            quality = "png".equals(imageFormat) ? 0f : 1f;
        }

        if (dpi == 0) {
            try {
                dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            } catch (HeadlessException e) {
                dpi = 96;
            }
        }

        try (PDDocument document = Loader.loadPDF(file, password)) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null && acroForm.getNeedAppearances()) {
                acroForm.refreshAppearances();
            }

            if (cropbox != null) {
                changeCropBox(document, cropbox[0], cropbox[1], cropbox[2], cropbox[3]);
            }

            endPage = Math.min(endPage, document.getNumberOfPages());
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(subsampling);
            PDDocument oDoc = new PDDocument();
            PDRectangle actualMediaBox = PDRectangle.A4;

            PDPage oInPage;
            for (int i = startPage - 1; i < endPage; i++) {
                oInPage = document.getPage(i);
                log.info("Input Page Height:{}", oInPage.getBBox().getHeight());
                log.info("Input Page Width:{}", oInPage.getBBox().getWidth());
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, imageType);
                ByteArrayOutputStream baosImage = new ByteArrayOutputStream();
                ImageIOUtil.writeImage(removeAlphaChannel(image),"jpg",baosImage, dpi, quality);

                PDPage oPage = new PDPage(actualMediaBox);
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(oDoc,baosImage.toByteArray(),null);
//                PDImageXObject pdImage = PDImageXObject.createFromFile(fileName, oDoc);
                log.info("Image Height:{}",pdImage.getHeight());
                log.info("Image Width:{}",pdImage.getWidth());
                try (PDPageContentStream contentStream = new PDPageContentStream(oDoc, oPage, AppendMode.APPEND, true, true)) {
                    float scale = Math.min((oPage.getBBox().getHeight() - iMargin * 2) / pdImage.getHeight(), (oPage.getBBox().getWidth() - iMargin * 2) / pdImage.getWidth());
                    contentStream.drawImage(pdImage, iMargin, iMargin, pdImage.getWidth() * scale, pdImage.getHeight() * scale);
                }
                oDoc.addPage(oPage);
            }
            oDoc.save("output.pdf");
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            oDoc.save(response);
            oDoc.close();

            return response;
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private static void changeCropBox(PDDocument document, float a, float b, float c, float d) {
        for (PDPage page : document.getPages()) {
            PDRectangle rectangle = new PDRectangle();
            rectangle.setLowerLeftX(a);
            rectangle.setLowerLeftY(b);
            rectangle.setUpperRightX(c);
            rectangle.setUpperRightY(d);
            page.setCropBox(rectangle);
        }
    }

    private static BufferedImage removeAlphaChannel(BufferedImage img) {
        if (!img.getColorModel().hasAlpha()) {
            return img;
        }

        BufferedImage target = createImage(img.getWidth(), img.getHeight());
        Graphics2D g = target.createGraphics();
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return target;
    }
    private static BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }
}
