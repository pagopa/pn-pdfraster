package it.pagopa.pn.pdfraster.utils.localstack;

import lombok.CustomLog;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SSM;

@TestConfiguration
@CustomLog
public class LocalStackTestConfig {

    private static DockerImageName dockerImageName = DockerImageName.parse("localstack/localstack:1.0.4");
    private static LocalStackContainer localStackContainer = new LocalStackContainer(dockerImageName).withServices(SSM)
                                                                                                     .withStartupTimeout(Duration.ofMinutes(2))
                                                                                                     .withEnv("AWS_DEFAULT_REGION","eu-south-1");

    static {
        localStackContainer.start();

        System.setProperty("test.aws.ssm.endpoint", String.valueOf(localStackContainer.getEndpointOverride(SSM)));

        ssmInit();
    }

    private static void ssmInit(){
        try {
            localStackContainer.execInContainer("awslocal",
                    "ssm",
                    "put-parameter",
                    "--name",
                    "pn-PDFRaster",
                    "--type",
                    "String",
                    "--value",
                    "{" +
                        "\"cropbox\":\"0,0,595,841\"," +
                        "\"dpi\":150," +
                        "\"margins\":\"0,0,595,841\"," +
                        "\"mediaSize\":\"A4\"," +
                        "\"scaleOrCrop\":\"scale\"," +
                        "\"maxFileSize\":10000000" +
                    "}");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
