package it.pagopa.pn.pdfraster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static it.pagopa.pn.pdfraster.utils.FontUtils.loadFonts;

@SpringBootApplication(scanBasePackages = {"it.pagopa.pn.pdfraster"})
@ConfigurationPropertiesScan(basePackages = {"it.pagopa.pn.pdfraster"})
// AWS CONFIGURATION
@PropertySource("classpath:commons/aws-configuration.properties")
// APPLICATION
@PropertySource("classpath:commons/pdfraster.properties")
public class PdfRasterApplication {


    public static void main(String[] args) {
        loadFonts();
        SpringApplication app = new SpringApplication(PdfRasterApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        app.run(args);
    }

}