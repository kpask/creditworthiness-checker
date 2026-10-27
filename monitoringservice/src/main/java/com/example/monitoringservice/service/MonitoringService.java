package com.example.monitoringservice.service;

import com.example.monitoringservice.client.BackendClient;
import com.example.monitoringservice.client.FlaggedRequest;
import com.example.monitoringservice.entity.ReportedRequest;
import com.example.monitoringservice.repository.ReportedRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MonitoringService {
    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);
    private final BackendClient backendClient;
    private final ReportedRequestRepository reportedRepo;
    private final ReportMailer mailer;

    public MonitoringService(BackendClient backendClient,
                             ReportedRequestRepository reportedRepo,
                             ReportMailer mailer) {
        this.backendClient = backendClient;
        this.reportedRepo = reportedRepo;
        this.mailer = mailer;
    }

    @Scheduled(cron = "${monitoring.cron}")
    public void run() {
        List<FlaggedRequest> flagged;
        try {
            flagged = backendClient.fetchFlagged();
        } catch (RestClientException e) {
            log.warn("Backend unavailable, will retry next run: {}", e.getMessage());
            return;
        }

        Set<UUID> alreadyReported = reportedRepo
                .findAllById(flagged.stream().map(FlaggedRequest::id).toList())
                .stream().map(ReportedRequest::getRequestId).collect(Collectors.toSet());

        List<FlaggedRequest> fresh = flagged.stream()
                .filter(r -> !alreadyReported.contains(r.id()))
                .toList();
        if (fresh.isEmpty()) return;

        try {
            mailer.send(fresh);
        } catch (MailException e) {
            log.error("Email failed, {} request(s) stay pending: {}", fresh.size(), e.getMessage());
            return; // nothing recorded, so the next run retries
        }

        reportedRepo.saveAll(fresh.stream()
                .map(r -> new ReportedRequest(r.id(), Instant.now()))
                .toList());
    }
}
