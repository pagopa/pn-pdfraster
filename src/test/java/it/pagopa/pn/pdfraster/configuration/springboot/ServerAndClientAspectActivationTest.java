package it.pagopa.pn.pdfraster.configuration.springboot;

import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTestWebEnv
@DirtiesContext
class ServerAndClientAspectActivationTest {

    @Autowired
    private ServerAspectActivation serverAspectActivation;

    @Autowired
    private ClientAspectActivation clientAspectActivation;

    @Test
    void serverAspectActivation_beanLoaded_notNull() {
        assertNotNull(serverAspectActivation);
    }

    @Test
    void clientAspectActivation_beanLoaded_notNull() {
        assertNotNull(clientAspectActivation);
    }
}
