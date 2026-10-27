package com.example.creditworthinesschecker.credit.dto;

import com.example.creditworthinesschecker.credit.enums.Country;
import com.example.creditworthinesschecker.credit.enums.IdCodeType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pcr.requester")
public record PcrRequesterProperties(
        IdCodeType idCodeType,
        String idCode,
        Country country
) {}
