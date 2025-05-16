package it.pagopa.pn.pdfraster.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsConfigurationProperties(String regionCode) {
}
