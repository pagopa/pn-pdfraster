package it.pagopa.pn.pdfraster.configuration.aws;


import it.pagopa.pn.pdfraster.model.pojo.PdfTransformationConfigParams;
import it.pagopa.pn.pdfraster.model.pojo.TransformationEnum;
import it.pagopa.pn.pdfraster.utils.JsonUtils;
import lombok.CustomLog;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.pagopa.pn.pdfraster.utils.LogUtils.CLIENT_METHOD_INVOCATION_WITH_ARGS;

@Configuration
@CustomLog
public class PdfTransformationConfiguration {

    private final SsmClient ssmClient;
    private final JsonUtils jsonUtils;

    @Getter
    private final PdfTransformationConfigParams pdfTransformationConfigParams;

    private final String pdfRasterParameterName;
    @Getter
    private final List<TransformationEnum> transformationsList;


    public PdfTransformationConfiguration(SsmClient ssmClient, JsonUtils jsonUtils, @Value("${pn.pdfraster.parameter.name}") String pdfRasterParameterName, List<String> transformations) {
        this.ssmClient = ssmClient;
        this.jsonUtils = jsonUtils;
        this.pdfRasterParameterName = pdfRasterParameterName;
        this.pdfTransformationConfigParams = this.pdfTransformationConfigurationFromParameterStore();
        this.transformationsList = parseTransformations(pdfTransformationConfigParams.getTransformationsList());
    }

    public PdfTransformationConfigParams pdfTransformationConfigurationFromParameterStore() throws SsmException {

        log.debug(CLIENT_METHOD_INVOCATION_WITH_ARGS, "ssmClient.getParameters", pdfRasterParameterName);

        GetParameterResponse response = ssmClient.getParameter(GetParameterRequest.builder()
                .name(pdfRasterParameterName)
                .build());

        String parameterValue = response.parameter().value();

        return jsonUtils.convertJsonStringToObject(parameterValue, PdfTransformationConfigParams.class);

    }

    private List<TransformationEnum> parseTransformations(String transformations) {
        List<TransformationEnum> transformationsList = new ArrayList<>();
        boolean containsCropOrScale = false;
        for (String transformation : transformations.split(";")) {
            if (!Arrays.asList(TransformationEnum.values()).contains(TransformationEnum.getValue(transformation))) {
                throw new IllegalArgumentException("Transformation " + transformation + "not in Enum");
            } else {
                TransformationEnum currentTransformation = TransformationEnum.getValue(transformation);
                if (transformation!=null&&(currentTransformation.equals(TransformationEnum.CROP) || currentTransformation.equals(TransformationEnum.SCALE))) {
                    if (containsCropOrScale) {
                        throw new IllegalArgumentException("Cannot contain both scale and crop operations");
                    } else {
                        containsCropOrScale = true;
                    }
                }
                transformationsList.add(TransformationEnum.getValue(transformation));
            }
        }
        return transformationsList;
    }




}
