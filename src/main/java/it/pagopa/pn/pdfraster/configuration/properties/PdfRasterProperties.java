package it.pagopa.pn.pdfraster.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "pn.pdfraster")
@Configuration
@Data
public class PdfRasterProperties {
    private Sqs sqs;
    private Integer maxThreadPoolSize;

    @Data
    public static class Sqs {
        private Integer maxMessages;
        private RetryStrategy retryStrategy;

        @Data
        public static class RetryStrategy {
            private Long maxAttempts;
            private Long minBackoff;
        }
    }


}
