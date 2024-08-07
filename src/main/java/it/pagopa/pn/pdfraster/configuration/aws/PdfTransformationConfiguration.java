package it.pagopa.pn.pdfraster.configuration.aws;


import it.pagopa.pn.pdfraster.model.pojo.PdfTransformationConfigParams;
import it.pagopa.pn.pdfraster.utils.JsonUtils;
import lombok.CustomLog;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;


import static it.pagopa.pn.pdfraster.utils.LogUtils.CLIENT_METHOD_INVOCATION_WITH_ARGS;


@Configuration
@CustomLog
public class PdfTransformationConfiguration {

    private final SsmClient ssmClient;
    private final JsonUtils jsonUtils;

    @Getter
    private final PdfTransformationConfigParams pdfTransformationConfigParams;

    @Value("${pn.pdfraster.parameter.name}")
    private String pdfRasterParameterName;



    public PdfTransformationConfiguration(SsmClient ssmClient, JsonUtils jsonUtils) {
        this.ssmClient = ssmClient;
        this.jsonUtils = jsonUtils;
        this.pdfTransformationConfigParams = this.pdfTransformationConfigurationFromParameterStore();

    }

    public PdfTransformationConfigParams pdfTransformationConfigurationFromParameterStore() throws SsmException {

        log.info(CLIENT_METHOD_INVOCATION_WITH_ARGS, "ssmClient.getParameters", pdfRasterParameterName);

        GetParameterResponse response = ssmClient.getParameter(GetParameterRequest.builder()
                .name(pdfRasterParameterName)
                .withDecryption(true)
                .build());

        String parameterValue = response.parameter().value();

        return  jsonUtils.convertJsonStringToObject(parameterValue, PdfTransformationConfigParams.class);

    }


}
