package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.configuration.aws.PdfTransformationConfiguration;
import it.pagopa.pn.pdfraster.exceptions.Generic500ErrorException;
import it.pagopa.pn.pdfraster.model.pojo.MediaSizeWrapper;
import it.pagopa.pn.pdfraster.model.pojo.PdfTransformationConfigParams;
import it.pagopa.pn.pdfraster.model.pojo.TransformationEnum;
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
import org.springframework.stereotype.Service;
import picocli.CommandLine.Command;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static it.pagopa.pn.pdfraster.utils.LogUtils.*;
import static it.pagopa.pn.pdfraster.utils.PDFUtils.*;
import static it.pagopa.pn.pdfraster.utils.FontProvider.*;

@CustomLog
@Service
@Command(name = "pdftoimage", header = "Converts a PDF document to image(s)", mixinStandardHelpOptions = true)
public class ConvertPdfServiceImpl implements ConvertPdfService {

    private static final String IMAGE_FORMAT = "png";
    private final ImageType imageType;
    private int dpi;
    private final Integer[] margins;
    private final Integer[] cropbox;
    private final PDRectangle mediaSize;
    private final List<TransformationEnum> transformations;


    public ConvertPdfServiceImpl(PdfTransformationConfiguration pdfTransformationConfiguration){
        PdfTransformationConfigParams params = pdfTransformationConfiguration.getPdfTransformationConfigParams();
        this.cropbox = Arrays.stream(params.getCropbox().split(",")).map(a -> Integer.parseInt(a.trim())).toArray(Integer[]::new);
        this.dpi = (int)params.getDpi();
        this.margins = Arrays.stream(params.getMargins().split(",")).map(a -> Integer.parseInt(a.trim())).toArray(Integer[]::new);
        this.mediaSize = MediaSizeWrapper.getMediaSize(params.getMediaSize());
        this.transformations = pdfTransformationConfiguration.getTransformationsList();
        this.imageType = params.isConvertToGrayscale() ? ImageType.GRAY : ImageType.ARGB;
        log.debug("cropbox= {},margins= {}, dpi= {}, mediasize= {}, transformations= {}, isConvertToGrayScale={} ", params.getCropbox(),params.getMargins(),params.getDpi(),params.getMediaSize(),params.getTransformationsList(), params.isConvertToGrayscale());
    }

    @Override
    public Mono<ByteArrayOutputStream> convertPdfToImage(byte[] file) {
        if (dpi == 0) {
            dpi = 96;
        }
        return Mono.fromCallable(() -> Loader.loadPDF(file))
                .flatMap(pdDocument -> {
                    loadFonts(pdDocument);
                    preliminarOperations(pdDocument);
                    PDFRenderer renderer = new PDFRenderer(pdDocument);
                    renderer.setSubsamplingAllowed(false);
                    int numberOfPages = pdDocument.getNumberOfPages();
                    return Flux.range(0, numberOfPages)
                            .flatMap(pageIndex -> processPage(pdDocument, pageIndex,renderer))
                            .cast(BufferedImage.class)
                            .map(this::transformations)
                            .reduce(new PDDocument(), this::addImageToPdf)
                            .map(this::saveDocument);
                })
                .doOnSuccess(byteArrayOutputStream -> log.info(SUCCESSFUL_OPERATION_NO_RESULT_LABEL,CONVERT_PDF_TO_IMAGE))
                .doOnError(throwable -> log.error(ENDING_PROCESS_WITH_ERROR,CONVERT_PDF_TO_IMAGE,throwable,throwable.getMessage()));
    }

    /*
     * Le trasformazioni possibili sono:
     * crop: taglia l'immagine in base quanto impostato nel parametro "cropbox"
     * scale: l'immagine viene scalata in modo da farla rientrare nei margini previsti
     *   Questa trasformazione viene comunque eseguita sempre come ultima trasformazione dell'elenco
     * portrait: se l'immagine ha la base maggiore dell'altezza questa viene ruotata di 90° in senso orario
     * 
     * è possibile specificare una sequenza di trasformazioni, di seguito gli effetti:
     * - portrait;crop: l'immagine viene prima, se necessario, ruotata e poi tagliata
     * - portrait;scale: l'immagine viene prima, se necessario, ruotata e poi scalata
     * - crop;portrait: l'immagine viene tagliata e se necessario ruotata
     * - scale;portrait: è equivalente alla trasformazione portrait;scale in quanto la scalatura avviene sempre applicata per ultima
     */
    private BufferedImage transformations(BufferedImage bImage) {
        int pageH = margins[3]-margins[1];
        int pageW = margins[2]-margins[0];

        log.debug("Image Height: {}, Image Width: {}", bImage.getHeight(),bImage.getWidth());

        for (TransformationEnum transformation : transformations) {
            switch (transformation) {
                case CROP:
                    bImage = cropImage(bImage,dpi,cropbox,pageH,pageW);
                    break;
                case PORTRAIT:
                    bImage = rotateImage(bImage, 90);
                    break;
            }
        }
        return bImage;
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


        } catch (IOException e) {
            throw new Generic500ErrorException("Error while converting pdf", e.getMessage());
        }
    }

    /**
     * Metodo per la creazione del Pdf con le immagini create
     * @return
     */
    private PDDocument addImageToPdf(PDDocument oDoc, BufferedImage image) {
        try {
            PDPage oPage = new PDPage(mediaSize);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIOUtil.writeImage(image, IMAGE_FORMAT, baos, dpi);

            PDImageXObject pdImage = PDImageXObject.createFromByteArray(oDoc,baos.toByteArray(), null);

            try (PDPageContentStream contentStream = new PDPageContentStream(oDoc, oPage, AppendMode.APPEND, true, true)) {
                float scale = getScaleFactor(pdImage);
                log.debug("valore scale:{}", scale);
                contentStream.drawImage(pdImage, margins[0], margins[1], pdImage.getWidth()*scale, pdImage.getHeight()*scale);
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
    private Mono<BufferedImage> processPage(PDDocument pdDocument, Integer pageIndex, PDFRenderer renderer) {
        return Mono.fromCallable(() -> {
            PDPage oInPage = pdDocument.getPage(pageIndex);
            log.debug("Input Page Height: {}", oInPage.getBBox().getHeight());
            log.debug("Input Page Width: {}", oInPage.getBBox().getWidth());

            return renderer.renderImageWithDPI(pageIndex, dpi, imageType);
        })
        .doOnSuccess(bytes -> log.info(SUCCESSFUL_OPERATION_ON_LABEL,PROCESS_PAGE,PAGE_INDEX,pageIndex))
        .doOnError(throwable -> log.error(ENDING_PROCESS_WITH_ERROR,PROCESS_PAGE,throwable,throwable.getMessage()));
    }


    /**
     * Metodo per il calcolo dello scale dell'immagine in base alla variabile scaleOrCrop
     * @param pdImage
     * @return
     */
    private float getScaleFactor(PDImageXObject pdImage) {
        float scale = 72f / dpi;
        if (transformations.contains(TransformationEnum.SCALE)) {
            scale = Math.min((float)(margins[3]-margins[1])/ pdImage.getHeight(), (float)(margins[2]-margins[0])/ pdImage.getWidth());
        }
        return scale;
    }

}
