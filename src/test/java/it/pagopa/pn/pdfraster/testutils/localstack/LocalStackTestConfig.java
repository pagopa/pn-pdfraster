package it.pagopa.pn.pdfraster.testutils.localstack;

import lombok.CustomLog;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;

@TestConfiguration
@CustomLog
public class LocalStackTestConfig {

    private static DockerImageName dockerImageName = DockerImageName.parse("localstack/localstack:1.0.4");
    private static LocalStackContainer localStackContainer = new LocalStackContainer(dockerImageName).withServices(SQS,SSM)
                                                                                                     .withStartupTimeout(Duration.ofMinutes(2))
                                                                                                     .withEnv("AWS_DEFAULT_REGION","eu-south-1");

    static {
        localStackContainer.start();

        System.setProperty("test.aws.ssm.endpoint", String.valueOf(localStackContainer.getEndpointOverride(SSM)));
        System.setProperty("test.aws.sqs.endpoint", String.valueOf(localStackContainer.getEndpointOverride(SQS)));

        ssmInit();
        sqsInit();
    }

    private static void ssmInit(){
        try {
            localStackContainer.execInContainer("awslocal",
                    "ssm",
                    "put-parameter",
                    "--name",
                    "pn-PdfRaster-settings",
                    "--type",
                    "String",
                    "--value",
                    "{\"cropbox\":\"0,0;1000,1000\",\"dpi\":150,\"mediaSize\":\"A4\",\"margins\":\"0,0;1500,2000\",\"scaleOrCrop\":\"crop\"}");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sqsInit(){
        log.info("<-- START initLocalStack.initSqs -->");

        List<String> queueNames = List.of("pn-pdf-raster-lavorazione-queue");

        for(String queue : queueNames){
            try {
                localStackContainer.execInContainer( "awslocal",
                        "sqs",
                        "create-queue",
                        "--queue-name",
                        queue
                );
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
