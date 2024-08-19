package it.pagopa.pn.pdfraster.utils;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RestClientsDetails {

    private final RestTemplate restTemplate;

    private static final String CONVERT_PDF_PATH = "/pdf-raster/convert";

    public RestClientsDetails(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }

    public Resource convertPdf(MultipartFile file) {
        return restTemplate.postForObject(CONVERT_PDF_PATH,file,Resource.class);
    }
}
