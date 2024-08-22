package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.configuration.aws.PdfTransformationConfiguration;
import it.pagopa.pn.pdfraster.exceptions.Generic500ErrorException;
import it.pagopa.pn.pdfraster.model.pojo.MediaSizeWrapper;
import it.pagopa.pn.pdfraster.model.pojo.PdfTransformationConfigParams;
import it.pagopa.pn.pdfraster.model.pojo.ScaleOrCropEnum;
import it.pagopa.pn.pdfraster.service.ConvertPdfService;
import lombok.CustomLog;
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
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

@CustomLog
@Service
@Command(name = "pdftoimage", header = "Converts a PDF document to image(s)", mixinStandardHelpOptions = true)
public class ConvertPdfServiceImpl implements ConvertPdfService {

    private static final String IMAGE_FORMAT = "png";
    private static final ImageType IMAGE_TYPE = ImageType.ARGB;
    private int dpi;
    private final Integer[] margins;
    private final Integer[] cropbox;
    private final PDRectangle mediaSize;
    private final ScaleOrCropEnum scaleOrCrop;


    public ConvertPdfServiceImpl(PdfTransformationConfiguration pdfTransformationConfiguration){
        PdfTransformationConfigParams params = pdfTransformationConfiguration.getPdfTransformationConfigParams();
        this.cropbox = Arrays.stream(params.getCropbox().split(",")).map(Integer::parseInt).toArray(Integer[]::new);
        this.dpi = (int)params.getDpi();
        this.margins = Arrays.stream(params.getMargins().split(",")).map(Integer::parseInt).toArray(Integer[]::new);
        this.mediaSize = MediaSizeWrapper.getMediaSize(params.getMediaSize());
        this.scaleOrCrop = ScaleOrCropEnum.getValue(params.getScaleOrCrop());
    }

    @Override
    public ByteArrayOutputStream convertPdfToImage(byte[] file) {
        if (dpi == 0) {
            dpi = 96;
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null && acroForm.getNeedAppearances()) {
                acroForm.refreshAppearances();
            }

            if (cropbox != null) {
                changeCropBox(document, cropbox[0], cropbox[1], cropbox[2], cropbox[3]);
            }

            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(false);
            PDDocument oDoc = new PDDocument();

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDPage oInPage = document.getPage(i);
                log.info("Input Page Height: {}", oInPage.getBBox().getHeight());
                log.info("Input Page Width: {}", oInPage.getBBox().getWidth());
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, IMAGE_TYPE);

                ByteArrayOutputStream baosImage = new ByteArrayOutputStream();
                ImageIOUtil.writeImage(image, IMAGE_FORMAT, baosImage, dpi, 0f);

                PDPage oPage = new PDPage(mediaSize);
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(oDoc, baosImage.toByteArray(), null);

                try (PDPageContentStream contentStream = new PDPageContentStream(oDoc, oPage, AppendMode.APPEND, true, true)) {
                    float scale = getScaleOrCrop(pdImage);
                    contentStream.drawImage(pdImage, margins[0], margins[1], (margins[2]-margins[0]) * scale, (margins[3]-margins[1]) * scale);
                }

                oDoc.addPage(oPage);
            }

            oDoc.save("output.pdf");
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            oDoc.save(response);
            oDoc.close();

            return response;
        } catch (IOException e) {
            throw new Generic500ErrorException("Error while converting pdf", e.getMessage());
        }
    }

    /**
     * Metodo per il calcolo dello scale dell'immagine in base alla variabile scaleOrCrop
     * @param pdImage
     * @return
     */
    private float getScaleOrCrop(PDImageXObject pdImage) {
        float scale;
        if(ScaleOrCropEnum.CROP.equals(scaleOrCrop)){
            scale = Math.min((margins[3]-margins[1])/mediaSize.getHeight(), (margins[2]-margins[0])/mediaSize.getWidth());
        } else {
            scale = Math.min((float)(margins[3]-margins[1])/ pdImage.getHeight(), (float)(margins[2]-margins[0])/ pdImage.getWidth());
        }
        return scale;
    }

    /**
     *
     * @param document
     * @param a
     * @param b
     * @param c
     * @param d
     */
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
}
