
package com.example.creditworthinesschecker.credit;

import com.example.creditworthinesschecker.credit.enums.CreditRegisterExtractPurpose;
import com.example.creditworthinesschecker.financing.enums.LoanPurposeOfUse;
import com.example.creditworthinesschecker.financing.enums.LoanType;

public final class CreditRegisterExtractPurposeMapper {

    private CreditRegisterExtractPurposeMapper() {
    }

    public static CreditRegisterExtractPurpose map(LoanType loanType, LoanPurposeOfUse loanPurposeOfUse) {
        if (loanType == LoanType.GuaranteeReceivable) {
            return CreditRegisterExtractPurpose.GuaranteeOrThirdPartyPledge;
        }

        return switch (loanPurposeOfUse) {
            case OtherConsumerCredit,
                 LoanForPurchaseOfVehicleOrCraft ->
                    CreditRegisterExtractPurpose.NewConsumerCredit;

            case HomeLoan,
                 HomeLoanForFirstHome,
                 HomeLoanForLeisureHouse,
                 HomeLoanForInvestmentPurposes,
                 StudentLoan,
                 OtherLoan,
                 LoanForBusinessActivities,
                 GuaranteeReceivable ->
                    CreditRegisterExtractPurpose.NewLoan;
        };
    }
}
