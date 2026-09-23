package it.pagopa.pn.pdfraster.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "pn.pdfraster")
@Configuration
@Data
public class PnPdfRasterConfig {
    private String parameterName;
    private Integer maxThreadPoolSize;
    private Integer maxTransformationRetry;
    private Sqs sqs;

    @Data
    public static class Sqs {
        private String transformationQueueName;
        private Integer maxMessages;
        private RetryStrategy retryStrategy;

        @Data
        public static class RetryStrategy {
            private Long maxAttempts;
            private Long minBackoff;
        }
    }
}
