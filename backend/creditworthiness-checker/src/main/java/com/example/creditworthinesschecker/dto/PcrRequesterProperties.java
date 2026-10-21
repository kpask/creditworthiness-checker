package com.example.creditworthinesschecker.dto;

import com.example.creditworthinesschecker.enums.Country;
import com.example.creditworthinesschecker.enums.IdCodeType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pcr.requester")
public record PcrRequesterProperties(
        IdCodeType idCodeType,
        String idCode,
        Country country
) {}
