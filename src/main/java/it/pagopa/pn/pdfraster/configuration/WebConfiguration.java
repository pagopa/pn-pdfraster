package it.pagopa.pn.pdfraster.configuration;

import it.pagopa.pn.pdfraster.configuration.aws.PdfTransformationConfiguration;
import it.pagopa.pn.pdfraster.model.pojo.PdfTransformationConfigParams;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class WebConfiguration implements WebFluxConfigurer {

    private PdfTransformationConfigParams params;

    public WebConfiguration(PdfTransformationConfiguration pdfTransformationConfiguration){
        this.params = pdfTransformationConfiguration.getPdfTransformationConfigParams();
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        int maxFileSize = params.getMaxFileSize();
        var partReader = new DefaultPartHttpMessageReader();
        partReader.setMaxParts(1);
        partReader.setMaxInMemorySize(maxFileSize);
        MultipartHttpMessageReader multipartReader = new MultipartHttpMessageReader(partReader);
        configurer.defaultCodecs().multipartReader(multipartReader);
        configurer.defaultCodecs().maxInMemorySize(maxFileSize);
    }
}
