package it.pagopa.pn.pdfraster.utils.localstack;

import lombok.CustomLog;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;
@TestPropertySource("classpath:application-test.properties")
@TestConfiguration
@CustomLog
public class LocalStackTestConfig {

    static DockerImageName dockerImageName = DockerImageName.parse("localstack/localstack:1.0.4");
    static LocalStackContainer localStackContainer =
            new LocalStackContainer(dockerImageName).withServices(SSM, SQS, S3)
                    .withClasspathResourceMapping("testcontainers/init.sh", "/docker-entrypoint-initaws.d/make-storages.sh", BindMode.READ_ONLY)
                    .withClasspathResourceMapping("testcontainers/credentials", "/root/.aws/credentials", BindMode.READ_ONLY)
                    .withNetworkAliases("localstack")
                    .withNetwork(Network.builder().build())
                    .waitingFor(Wait.forLogMessage(".*Initialization terminated.*", 1));

    static {
        localStackContainer.start();

// Imposta le system properties per gli endpoint
        System.setProperty("test.aws.ssm.endpoint", localStackContainer.getEndpointOverride(SSM).toString());
        System.setProperty("test.aws.sqs.endpoint", localStackContainer.getEndpointOverride(SQS).toString());
        System.setProperty("cloud.aws.sqs.endpoint", localStackContainer.getEndpointOverride(SQS).toString());
        System.setProperty("test.aws.s3.endpoint", localStackContainer.getEndpointOverride(S3).toString());

        // Crea SSM client verso LocalStack
        SsmClient ssmClient = SsmClient.builder()
                                       .endpointOverride(localStackContainer.getEndpointOverride(SSM))
                                       .region(Region.of(localStackContainer.getRegion()))
                                       .credentialsProvider(
                                               StaticCredentialsProvider.create(AwsBasicCredentials.create("TEST", "TEST"))
                                                           )
                                       .build();

        PutParameterRequest putRequest = PutParameterRequest.builder()
                                                            .name("pn-PDFRaster")
                                                            .value("dummy-value")
                                                            .type("String")
                                                            .overwrite(true)
                                                            .build();

        ssmClient.putParameter(putRequest);

// LOG: verifica che sia stato creato correttamente
        log.info("SSM Parameter created: {}", putRequest.name());

        try {
            GetParameterResponse response = ssmClient.getParameter(GetParameterRequest.builder()
                                                                                      .name("pn-PDFRaster")
                                                                                      .build()
                                                                  );
            log.info("SSM Parameter value: {}", response.parameter().value());
        } catch (ParameterNotFoundException e) {
            log.error("Parameter pn-PDFRaster NON trovato!", e);
        }

    }

}
