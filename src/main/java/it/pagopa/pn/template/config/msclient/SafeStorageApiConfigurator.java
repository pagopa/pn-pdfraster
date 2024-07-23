package it.pagopa.pn.template.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.template.config.PnTemplateConfigs;
import it.pagopa.pn.template.generated.openapi.msclient.safestorage.ApiClient;
import it.pagopa.pn.template.generated.openapi.msclient.safestorage.api.FileDownloadApi;
import it.pagopa.pn.template.generated.openapi.msclient.safestorage.api.FileMetadataUpdateApi;
import it.pagopa.pn.template.generated.openapi.msclient.safestorage.api.FileUploadApi;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SafeStorageApiConfigurator extends CommonBaseClient {

    @Bean
    public FileUploadApi fileUploadApi(PnTemplateConfigs cfg){
        return new FileUploadApi( getNewApiClient(cfg) );
    }

    @Bean
    public FileDownloadApi fileDownloadApi(PnTemplateConfigs cfg){
        return new FileDownloadApi( getNewApiClient(cfg) );
    }

    @Bean
    public FileMetadataUpdateApi fileMetadataUpdateApi(PnTemplateConfigs cfg){
        return new FileMetadataUpdateApi( getNewApiClient(cfg) );
    }
    
    @NotNull
    private ApiClient getNewApiClient(PnTemplateConfigs cfg) {
        ApiClient newApiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder()) );
        newApiClient.setBasePath( cfg.getSafeStorageBaseUrl() );
        return newApiClient;
    }
}
