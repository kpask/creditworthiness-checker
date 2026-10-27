package com.example.creditworthinesschecker.credit;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditRepository extends JpaRepository<CreditExtract, UUID> {
    List<CreditExtract> findAllByPersonIdCodeOrderByFetchedAtDesc(String personIdCode);
    Optional<CreditExtract> findFirstByPersonIdCodeOrderByFetchedAtDesc(@NotEmpty String personIdCode);
}
