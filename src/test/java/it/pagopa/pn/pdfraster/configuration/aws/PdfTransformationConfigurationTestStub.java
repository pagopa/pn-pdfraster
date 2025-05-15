package it.pagopa.pn.pdfraster.configuration.aws;

import it.pagopa.pn.pdfraster.model.pojo.PdfTransformationConfigParams;
import it.pagopa.pn.pdfraster.model.pojo.TransformationEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("test") // Solo quando il profilo attivo è "test"
public class PdfTransformationConfigurationTestStub extends PdfTransformationConfiguration {

    public PdfTransformationConfigurationTestStub() {
        // Passiamo null a dipendenze inutilizzate, e valori fittizi per i parametri
        super(null, null, "mock", List.of("SCALE"));
    }

    @Override
    public PdfTransformationConfigParams pdfTransformationConfigurationFromParameterStore() {
        // Costruzione di parametri di test realistici
        return PdfTransformationConfigParams.builder()
                                            .cropbox("0,0,595,841") // ✅ Formato corretto
                                            .dpi(300)
                                            .mediaSize("A4")
                                            .margins("0,0,595,841")
                                            .transformationsList("portrait;scale")
                                            .maxFileSize(100000000)
                                            .convertToGrayscale(false)
                                            .build();
    }

    @Override
    public List<TransformationEnum> getTransformationsList() {
        return List.of(TransformationEnum.SCALE);
    }
}
