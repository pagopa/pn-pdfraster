package it.pagopa.pn.pdfraster.configuration;

import it.pagopa.pn.pdfraster.configuration.aws.PdfTransformationConfiguration;
import it.pagopa.pn.pdfraster.model.pojo.PdfTransformationConfigParams;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfiguration {

    @Bean
    public MultipartConfigElement multipartConfigElement(PdfTransformationConfiguration conf) {
        PdfTransformationConfigParams params = conf.getPdfTransformationConfigParams();
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(params.getMaxFileSize()));
        factory.setMaxRequestSize(DataSize.ofBytes(params.getMaxFileSize() + 1000));
        return factory.createMultipartConfig();
    }
}
