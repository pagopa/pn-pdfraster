package it.pagopa.pn.pdfraster.configuration;

import it.pagopa.pn.pdfraster.configuration.properties.AwsConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;

import java.net.URI;

@Configuration
public class AwsConfiguration {

    private final AwsConfigurationProperties awsConfigurationProperties;

    @Value("${test.aws.sqs.endpoint:#{null}}")
    String sqsLocalStackEndpoint;

    @Value("${test.aws.ssm.endpoint:#{null}}")
    String ssmLocalStackEndpoint;

    private static final DefaultCredentialsProvider DEFAULT_CREDENTIALS_PROVIDER_V2 = DefaultCredentialsProvider.create();

    public AwsConfiguration(AwsConfigurationProperties awsConfigurationProperties) {
        this.awsConfigurationProperties = awsConfigurationProperties;
    }

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        SqsAsyncClientBuilder sqsAsyncClientBuilder = SqsAsyncClient.builder()
                .credentialsProvider(DEFAULT_CREDENTIALS_PROVIDER_V2)
                .region(Region.of(awsConfigurationProperties.regionCode()));

        if (sqsLocalStackEndpoint != null) {
            sqsAsyncClientBuilder.endpointOverride(URI.create(sqsLocalStackEndpoint));
        }

        return sqsAsyncClientBuilder.build();
    }

    @Bean
    public SsmClient ssmClient() {
        SsmClientBuilder ssmClientBuilder = SsmClient.builder()
                .credentialsProvider(DEFAULT_CREDENTIALS_PROVIDER_V2)
                .region(Region.of(awsConfigurationProperties.regionCode()));

        if(ssmLocalStackEndpoint != null) {
            ssmClientBuilder.endpointOverride(URI.create(ssmLocalStackEndpoint));
        }

        return ssmClientBuilder.build();
    }

}
