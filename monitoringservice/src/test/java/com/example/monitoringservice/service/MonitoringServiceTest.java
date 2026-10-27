package com.example.monitoringservice.service;

import com.example.monitoringservice.client.BackendClient;
import com.example.monitoringservice.client.FlaggedRequest;
import com.example.monitoringservice.entity.ReportedRequest;
import com.example.monitoringservice.repository.ReportedRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class MonitoringServiceTest {

    private final BackendClient backendClient = mock(BackendClient.class);
    private final ReportedRequestRepository reportedRepo = mock(ReportedRequestRepository.class);
    private final ReportMailer mailer = mock(ReportMailer.class);
    private final MonitoringService service = new MonitoringService(backendClient, reportedRepo, mailer);

    @Test
    void runDoesNotPersistWhenEmailFailsSoNextRunCanRetry() {
        FlaggedRequest flagged = sampleFlagged(UUID.randomUUID());
        when(backendClient.fetchFlagged()).thenReturn(List.of(flagged));
        when(reportedRepo.findAllById(anyList())).thenReturn(List.of());
        doThrow(new MailSendException("SMTP down")).when(mailer).send(any());

        service.run();

        verify(mailer).send(List.of(flagged));
        verify(reportedRepo, never()).saveAll(any());
    }

    @Test
    void runPersistsAfterSuccessfulEmail() {
        FlaggedRequest flagged = sampleFlagged(UUID.randomUUID());
        when(backendClient.fetchFlagged()).thenReturn(List.of(flagged));
        when(reportedRepo.findAllById(anyList())).thenReturn(List.of());

        service.run();

        verify(mailer).send(List.of(flagged));
        verify(reportedRepo).saveAll(any());
    }

    @Test
    void runSkipsAlreadyReportedRequests() {
        UUID id = UUID.randomUUID();
        FlaggedRequest flagged = sampleFlagged(id);
        when(backendClient.fetchFlagged()).thenReturn(List.of(flagged));
        when(reportedRepo.findAllById(anyList()))
                .thenReturn(List.of(new ReportedRequest(id, Instant.now())));

        service.run();

        verify(mailer, never()).send(any());
        verify(reportedRepo, never()).saveAll(any());
    }

    @Test
    void runReturnsWhenBackendUnavailable() {
        when(backendClient.fetchFlagged()).thenThrow(new RestClientException("backend down"));

        service.run();

        verifyNoInteractions(mailer);
        verify(reportedRepo, never()).saveAll(any());
    }

    private static FlaggedRequest sampleFlagged(UUID id) {
        return new FlaggedRequest(
                id,
                "TEST-002",
                "LumpSumLoan",
                "OtherConsumerCredit",
                Instant.now(),
                "CHECKED",
                true
        );
    }
}
