package it.pagopa.pn.pdfraster.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pn.pdfraster.sqs.retry.strategy")
public record SqsRetryStrategyProperties(Long maxAttempts, Long minBackoff) {
}
