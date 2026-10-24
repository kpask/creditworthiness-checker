package com.example.monitoringservice.repository;

import com.example.monitoringservice.entity.ReportedRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportedRequestRepository extends JpaRepository<ReportedRequest, UUID> {
}
