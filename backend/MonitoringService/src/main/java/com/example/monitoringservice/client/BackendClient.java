package com.example.monitoringservice.client;

import com.example.monitoringservice.config.MonitoringProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class BackendClient {
    private final RestClient restClient;

    public BackendClient(MonitoringProperties props) {
        this.restClient = RestClient.builder()
                .baseUrl(props.backendUrl())
                .build();
    }

    public List<FlaggedRequest> fetchFlagged() {
        return restClient.get()
                .uri("/financing-requests/flagged")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
