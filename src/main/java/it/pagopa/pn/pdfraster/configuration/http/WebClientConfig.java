package it.pagopa.pn.pdfraster.configuration.http;

import it.pagopa.pn.pdfraster.configuration.properties.SafeStorageEndpointProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration
public class WebClientConfig {

    private final JettyHttpClientConf jettyHttpClientConf;

    public WebClientConfig(JettyHttpClientConf jettyHttpClientConf) {
        this.jettyHttpClientConf = jettyHttpClientConf;
    }

    private WebClient.Builder defaultWebClientBuilder() {
        return WebClient.builder().clientConnector(new JettyClientHttpConnector(jettyHttpClientConf.getJettyHttpClient()));
    }

    private WebClient.Builder defaultJsonWebClientBuilder() {
        return defaultWebClientBuilder().defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
    }

    @Bean
    public WebClient ssWebClient(SafeStorageEndpointProperties safeStorageEndpointProperties) {
        return defaultJsonWebClientBuilder().baseUrl(safeStorageEndpointProperties.containerBaseUrl()).defaultHeaders(httpHeaders -> {
            httpHeaders.set(safeStorageEndpointProperties.clientHeaderName(), safeStorageEndpointProperties.clientHeaderValue());
            httpHeaders.set(safeStorageEndpointProperties.apiKeyHeaderName(), safeStorageEndpointProperties.apiKeyHeaderValue());
        }).build();
    }
}
