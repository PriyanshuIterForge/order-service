package com.example.ct_order_demo.config;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.defaultconfig.ApiRootBuilder;
import com.commercetools.api.defaultconfig.ServiceRegion;
import io.vrap.rmf.base.client.oauth2.ClientCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CTClientConfig {

    @Value("${ct.project-key}")
    private String projectKey;

    @Value("${ct.client-id}")
    private String clientId;

    @Value("${ct.client-secret}")
    private String clientSecret;

    @Bean
    public ProjectApiRoot apiRoot() {

        return ApiRootBuilder.of()
                .defaultClient(
                        ClientCredentials.of()
                                .withClientId(clientId)
                                .withClientSecret(clientSecret)
                                .build(),
                        ServiceRegion.GCP_EUROPE_WEST1
                )
                .build(projectKey);
    }
}
