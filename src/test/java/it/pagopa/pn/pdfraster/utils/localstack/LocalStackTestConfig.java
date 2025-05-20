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

        // Set system properties
        System.setProperty("test.aws.ssm.endpoint", String.valueOf(localStackContainer.getEndpointOverride(SSM)));

        // Crea SSM client verso LocalStack
        SsmClient ssmClient = SsmClient.builder()
                                       .endpointOverride(localStackContainer.getEndpointOverride(SSM))
                                       .region(Region.of(localStackContainer.getRegion()))
                                       .credentialsProvider(
                                               StaticCredentialsProvider.create(AwsBasicCredentials.create("TEST", "TEST"))
                                                           )
                                       .build();

        // Inserisci parametro richiesto
        ssmClient.putParameter(PutParameterRequest.builder()
                                                  .name("pn-PDFRaster")
                                                  .value("dummy-value")
                                                  .type("String")
                                                  .overwrite(true)
                                                  .build()
                              );
    }

}
