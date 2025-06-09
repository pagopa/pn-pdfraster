package it.pagopa.pn.pdfraster.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.messaging.config.QueueMessageHandlerFactory;
import io.awspring.cloud.messaging.listener.support.AcknowledgmentHandlerMethodArgumentResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.PayloadMethodArgumentResolver;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@Configuration
public class AwsConfiguration {

    @Value("${test.aws.region-code:#{null}}")
    String regionCode;

    @Value("${test.aws.sqs.endpoint:#{null}}")
    String sqsLocalStackEndpoint;

    @Value("${test.aws.ssm.endpoint:#{null}}")
    String ssmLocalStackEndpoint;

    @Value("${test.aws.s3.endpoint:#{null}}")
    private String testAwsS3Endpoint;

    @Value("${test.aws.cloudwatch.endpoint:#{null}}")
    private String testAwsCloudwatchEndpoint;

    private static final DefaultCredentialsProvider DEFAULT_CREDENTIALS_PROVIDER_V2 = DefaultCredentialsProvider.create();
    private final WebClient genericWebClient = WebClient.builder().build();


    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        SqsAsyncClientBuilder sqsAsyncClientBuilder = SqsAsyncClient.builder()
                .credentialsProvider(DEFAULT_CREDENTIALS_PROVIDER_V2);

        if (sqsLocalStackEndpoint != null) {
            sqsAsyncClientBuilder.endpointOverride(URI.create(sqsLocalStackEndpoint));
        }

        if(regionCode != null) {
            sqsAsyncClientBuilder.region(Region.of(regionCode));
        }

        return sqsAsyncClientBuilder.build();
    }

    @Bean
    public SsmClient ssmClient() {
        SsmClientBuilder ssmClientBuilder = SsmClient.builder()
                .credentialsProvider(DEFAULT_CREDENTIALS_PROVIDER_V2);

        if(ssmLocalStackEndpoint != null) {
            ssmClientBuilder.endpointOverride(URI.create(ssmLocalStackEndpoint));
        }

        if(regionCode != null) {
            ssmClientBuilder.region(Region.of(regionCode));
        }

        return ssmClientBuilder.build();
    }

    @Bean
    public S3AsyncClient s3AsyncClient() {
        S3AsyncClientBuilder s3Client = S3AsyncClient.builder()
                .credentialsProvider(DEFAULT_CREDENTIALS_PROVIDER_V2);

        if (testAwsS3Endpoint != null) {
            s3Client.endpointOverride(URI.create(testAwsS3Endpoint));
        }

        if(regionCode != null) {
            s3Client.region(Region.of(regionCode));
        }

        return s3Client.build();
    }

    @Bean
    public QueueMessageHandlerFactory queueMessageHandlerFactory(ObjectMapper objectMapper, LocalValidatorFactoryBean validator) {

        final var queueMessageHandlerFactory = new QueueMessageHandlerFactory();
        final var converter = new MappingJackson2MessageConverter();

        converter.setObjectMapper(objectMapper);
        converter.setStrictContentTypeMatch(false);

        final var acknowledgmentResolver = new AcknowledgmentHandlerMethodArgumentResolver("Acknowledgment");

        queueMessageHandlerFactory.setArgumentResolvers(List.of(acknowledgmentResolver,
                new PayloadMethodArgumentResolver(converter, validator)));

        return queueMessageHandlerFactory;
    }

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        CloudWatchAsyncClientBuilder cloudWatchAsyncClientBuilder = CloudWatchAsyncClient.builder().credentialsProvider(DEFAULT_CREDENTIALS_PROVIDER_V2);

        if (testAwsCloudwatchEndpoint != null) {
            cloudWatchAsyncClientBuilder.endpointOverride(URI.create(testAwsCloudwatchEndpoint));
        }

        if(regionCode != null) {
            cloudWatchAsyncClientBuilder.region(Region.of(regionCode));
        }

        return cloudWatchAsyncClientBuilder.build();
    }

}
