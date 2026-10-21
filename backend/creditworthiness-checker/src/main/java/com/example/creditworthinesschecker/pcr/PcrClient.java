package com.example.creditworthinesschecker.pcr;

import com.example.creditworthinesschecker.dto.PcrCreditRegisterExtractResponse;
import com.example.creditworthinesschecker.dto.PcrCreditExtractRequest;

public interface PcrClient {
    PcrCreditRegisterExtractResponse fetchExtract(PcrCreditExtractRequest request);
}
