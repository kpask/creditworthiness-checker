package com.example.creditworthinesschecker.financing;


import com.example.creditworthinesschecker.credit.enums.BanCheckStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FinancingRequestRepository extends JpaRepository<FinancingRequest, UUID> {
    @Query("""
    select f from FinancingRequest f
    where f.submittedAt > :after
      and (f.banCheckStatus = :failed
           or (f.banCheckStatus = :checked and f.voluntaryBanInEffect = true))
    order by f.submittedAt asc
    """)
    List<FinancingRequest> findFlagged(
            @Param("checked") BanCheckStatus checked,
            @Param("failed") BanCheckStatus failed,
            @Param("after") Instant after);
}
