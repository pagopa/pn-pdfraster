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

        private static void loadFonts() {
            String fontDestPath = System.getProperty("user.home") + "/.fonts/"; //cartella di destinazione
            File fontDir = new File(fontDestPath);

            if (!fontDir.exists() && !fontDir.mkdirs()) {
                System.err.println("Errore nella creazione della cartella font: " + fontDestPath);
                return;
            }

            try {
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();//classe spring per caricare le risorse dalla cartella resources con un pattern di ricerca (potremmo specificare .ttf)
                Resource[] resources = resolver.getResources("classpath:fonts/*.ttf");

                //per ogni font, lo copio nella cartella di destinazione
                for (Resource resource : resources) {
                    String fileName = resource.getFilename();
                    if (fileName != null) {
                        File destFile = new File(fontDir, fileName);
                        if (!destFile.exists()) { // evita sovrascrittura
                            try (InputStream inputStream = resource.getInputStream()) {
                                Files.copy(inputStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("Font copiati in: " + destFile.getAbsolutePath());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Errore nel caricamento dei font: " + e.getMessage());
            }
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