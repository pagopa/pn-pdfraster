package it.pagopa.pn.pdfraster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"it.pagopa.pn.pdfraster"})
@ConfigurationPropertiesScan(basePackages = {"it.pagopa.pn.pdfraster"})
// AWS CONFIGURATION
@PropertySource("classpath:commons/aws-configuration.properties")
// SAFE STORAGE
@PropertySource("classpath:ss/ss-configuration.properties")
public class PdfRasterApplication {


    public static void main(String[] args) {
        SpringApplication.run(PdfRasterApplication.class, args);
    }

    @RestController
    @RequestMapping("/")
    public static class RootController {

        @GetMapping("/")
        public String home() {
            return HttpStatus.OK.name();
        }
    }
}