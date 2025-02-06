package it.pagopa.pn.pdfraster.utils.localstack;

import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SSM;

@TestConfiguration
@CustomLog
public class LocalStackTestConfig {
    @Autowired
    private S3AsyncClient s3AsyncClient;

    private static DockerImageName dockerImageName = DockerImageName.parse("localstack/localstack:1.0.4");
    private static LocalStackContainer localStackContainer = new LocalStackContainer(dockerImageName).withServices(SSM)
                                                                                                     .withServices(S3)
                                                                                                     .withStartupTimeout(Duration.ofMinutes(2))
                                                                                                     .withEnv("AWS_DEFAULT_REGION","eu-south-1");

    static {
        localStackContainer.start();

        System.setProperty("test.aws.ssm.endpoint", String.valueOf(localStackContainer.getEndpointOverride(SSM)));
        System.setProperty("test.aws.s3.endpoint", String.valueOf(localStackContainer.getEndpointOverride(S3)));


        initS3(localStackContainer);
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
                        "\"transformationsList\":\"scale\"," +
                        "\"maxFileSize\":10000000," +
                        "\"convertToGrayscale\":false" +
                    "}");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private static void initS3(LocalStackContainer localStackContainer) {

        String objectLockConfiguration = "{\"ObjectLockEnabled\":\"Enabled\",\"Rule\":{\"DefaultRetention\":{\"Mode\":\"GOVERNANCE\",\"Days\":1}}}";
        String lifecycleRule = "{\"Rules\": [{\"ID\": \"MoveToGlacier\", \"Filter\": {\"Prefix\": \"\"}, \"Status\": \"Enabled\", \"Transitions\": [{\"Days\": 1, \"StorageClass\": \"GLACIER\"}]}]}";

        List<String> bucketNames = Arrays.asList("hot-bucket-test", "stage-bucket-test");

        bucketNames.forEach(bucket -> {
            try {
                log.info("<-- START S3 init-->");
                Container.ExecResult result = localStackContainer.execInContainer("awslocal", "s3api", "head-bucket", "--bucket", bucket);
                if (result.getStderr().contains("404")) {
                    execInContainer("awslocal", "s3api", "create-bucket", "--region", localStackContainer.getRegion(), "--bucket", bucket, "--object-lock-enabled-for-bucket");
                    execInContainer("awslocal", "s3api", "put-object-lock-configuration", "--bucket", bucket, "--object-lock-configuration", objectLockConfiguration);
                    execInContainer("awslocal", "s3api", "put-bucket-lifecycle-configuration", "--bucket", bucket, "--lifecycle-configuration", lifecycleRule);
                    log.info("New bucket " + bucket + " created on local stack S3");
                } else log.info("Bucket " + bucket + " already created on local stack S3");
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

    }


    private static void execInContainer(String... command) throws IOException, InterruptedException {
        Container.ExecResult result = localStackContainer.execInContainer(command);
        if (result.getExitCode() != 0) {
            throw new RuntimeException(result.toString());
        }
    }

}
