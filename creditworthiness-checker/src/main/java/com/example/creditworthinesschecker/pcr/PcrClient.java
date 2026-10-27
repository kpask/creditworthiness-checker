package com.example.creditworthinesschecker.pcr;

import com.example.creditworthinesschecker.credit.dto.PcrCreditRegisterExtractResponse;
import com.example.creditworthinesschecker.credit.dto.PcrCreditExtractRequest;

public interface PcrClient {
    PcrCreditRegisterExtractResponse fetchExtract(PcrCreditExtractRequest request);
}
