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
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        this.cropbox = Arrays.stream(params.getCropbox().split(",")).map(a -> Integer.parseInt(a.trim())).toArray(Integer[]::new);
        this.dpi = (int)params.getDpi();
        this.margins = Arrays.stream(params.getMargins().split(",")).map(a -> Integer.parseInt(a.trim())).toArray(Integer[]::new);
        this.mediaSize = MediaSizeWrapper.getMediaSize(params.getMediaSize());
        this.scaleOrCrop = ScaleOrCropEnum.getValue(params.getScaleOrCrop());
        log.debug("cropbox= {},margins= {}, dpi= {}, mediasize= {}, scaleOrCrop= {} ", params.getCropbox(),params.getMargins(),params.getDpi(),params.getMediaSize(),params.getScaleOrCrop());
    }

    @Override
    public ByteArrayOutputStream convertPdfToImage(byte[] file) {
        if (dpi == 0) {
            dpi = 96;
        }
        return Mono.fromCallable(() -> Loader.loadPDF(file))
                .flatMap(pdDocument -> {
                    preliminarOperations(pdDocument);
                    PDFRenderer renderer = new PDFRenderer(pdDocument);
                    renderer.setSubsamplingAllowed(false);
                    int numberOfPages = pdDocument.getNumberOfPages();
                    return Flux.range(0, numberOfPages)
                            .flatMap(pageIndex -> processPage(pdDocument, pageIndex,renderer))
                            .reduce(new PDDocument(), (doc, bytes) -> addImageToPdf(doc,bytes))
                            .map(this::saveDocument);
                }).block();
    }

    private @NotNull ByteArrayOutputStream saveDocument(PDDocument document) {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        try {
            document.save(response);
        } catch (IOException e) {
            throw new Generic500ErrorException("Error while save pdf", e.getMessage());
        }
        return response;
    }

    /**
     * Metodo per rendere il documento lavorabile
     * @param pdDocument
     */
    private void preliminarOperations(PDDocument pdDocument) {
        try {
            PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
            if (acroForm != null && acroForm.getNeedAppearances()) {
                acroForm.refreshAppearances();
            }

            if (cropbox != null) {
                changeCropBox(pdDocument, cropbox[0], cropbox[1], cropbox[2], cropbox[3]);
            }
        } catch (IOException e) {
            throw new Generic500ErrorException("Error while converting pdf", e.getMessage());
        }
    }

    /**
     * Metodo per la creazione del Pdf con le immagini create
     * @return
     */
    private PDDocument addImageToPdf(PDDocument oDoc, byte[] image) {
        try {
            PDPage oPage = new PDPage(mediaSize);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(oDoc, image, null);

            try (PDPageContentStream contentStream = new PDPageContentStream(oDoc, oPage, AppendMode.APPEND, true, true)) {
                float scale = getScaleOrCrop(pdImage);
                log.debug("valore scale:{}", scale);
                contentStream.drawImage(pdImage, margins[0], margins[1], (margins[2] - margins[0]), (margins[3] - margins[1]));
            }
            oDoc.addPage(oPage);

            return oDoc;
        } catch (IOException e) {
            throw new Generic500ErrorException("Error while converting pdf", e.getMessage());
        }
    }

    /**
     * Metodo per trasformare il contenuto delle pagine in immagini
     * @param pdDocument
     * @param pageIndex
     * @param renderer
     * @return
     */
    private Publisher<byte[]> processPage(PDDocument pdDocument, Integer pageIndex, PDFRenderer renderer) {
        return Mono.fromCallable(() -> {
            PDPage oInPage = pdDocument.getPage(pageIndex);
            log.debug("Input Page Height: {}", oInPage.getBBox().getHeight());
            log.debug("Input Page Width: {}", oInPage.getBBox().getWidth());
            BufferedImage image = renderer.renderImageWithDPI(pageIndex, dpi, IMAGE_TYPE);

            log.debug("Buffered Image size: width = {}, height = {}",image.getWidth(), image.getHeight());
            ByteArrayOutputStream baosImage = new ByteArrayOutputStream();
            ImageIOUtil.writeImage(image, IMAGE_FORMAT, baosImage, dpi, 0f);
            return baosImage.toByteArray();
        });
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
