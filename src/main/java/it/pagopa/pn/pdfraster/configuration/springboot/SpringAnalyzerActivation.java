package it.pagopa.pn.pdfraster.configuration.springboot;

import io.micrometer.core.instrument.MeterRegistry;
import it.pagopa.pn.commons.utils.metrics.SpringAnalyzer;
import it.pagopa.pn.commons.utils.metrics.cloudwatch.CloudWatchMetricHandler;
import lombok.CustomLog;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@CustomLog
@Import(CloudWatchMetricHandler.class)
public class SpringAnalyzerActivation extends SpringAnalyzer {

    MeterRegistry meterRegistry;
    CloudWatchMetricHandler cloudWatchMetricHandler;

    public SpringAnalyzerActivation(CloudWatchMetricHandler cloudWatchMetricHandler, MetricsEndpoint metricsEndpoint, MeterRegistry meterRegistry) {
        super(cloudWatchMetricHandler, metricsEndpoint);
        this.cloudWatchMetricHandler = cloudWatchMetricHandler;
        this.meterRegistry = meterRegistry;
    }

    // Override del metodo scheduledSendMetrics con logica di retry per gestire il caso in cui il client non riesce ad eseguire la richiesta perchè il thread si è chiuso
    @Override
    @Scheduled(cron = "${pn.analyzer.cloudwatch-metric-cron}")
    public void scheduledSendMetrics() {
        sendMetricsWithRetry(3, 1000); // 3 tentativi, 1 secondo di intervallo
    }

    public void sendMetricsWithRetry(int retries, long delayMillis) {
        int attempts = 0;
        while (attempts < retries && !isExecutorActive()) {
            try {
                log.info("Attempting to send metrics... Attempt #" + (attempts + 1));
                super.scheduledSendMetrics();  // chiama il metodo originale per inviare le metriche
                break;
            } catch (Exception e) {
                attempts++;
                log.error("Error sending metrics on attempt #" + attempts, e);
                if (attempts >= retries) {
                    log.error("Max retries reached. Metrics not sent.");
                }
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private boolean isExecutorActive() {
        // check se l'executor è ancora su
        return !Thread.currentThread().isInterrupted();
    }
}



