package it.pagopa.pn.pdfraster.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ss.endpoint")
public record SafeStorageEndpointProperties(String containerBaseUrl, String clientHeaderName, String clientHeaderValue,
                                            String apiKeyHeaderName, String apiKeyHeaderValue,
                                            String checksumValueHeaderName, String traceIdHeaderName,
                                            String getFile, String postFile) {
}

