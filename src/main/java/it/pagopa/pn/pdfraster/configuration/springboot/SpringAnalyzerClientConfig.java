package it.pagopa.pn.pdfraster.configuration.springboot;

import it.pagopa.pn.commons.configs.SpringAnalyzerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SpringAnalyzerClientConfig extends SpringAnalyzerConfiguration {}
