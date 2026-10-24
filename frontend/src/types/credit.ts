export type IdCodeType = 'PersonalIdentityCode' | 'BusinessId' | 'ForeignBusinessId';
export type CreditRegisterExtractPurpose =
  | 'NewConsumerCredit'
  | 'IncreaseOfConsumerCreditPrincipalOrCreditLimit'
  | 'ChangesToTermsOfConsumerCredit'
  | 'GuaranteeOrThirdPartyPledgeForConsumerCredit'
  | 'NewLoan'
  | 'IncreaseOfLoanPrincipalOrCreditLimit'
  | 'ChangesToTerms'
  | 'GuaranteeOrThirdPartyPledge';

export type LoanType = 'LumpSumLoan' | 'RunningAccountLoan' | 'Leasing' | 'GuaranteeReceivable';
export type LoanPurposeOfUse =
  | 'HomeLoan'
  | 'HomeLoanForFirstHome'
  | 'HomeLoanForLeisureHouse'
  | 'HomeLoanForInvestmentPurposes'
  | 'StudentLoan'
  | 'LoanForPurchaseOfVehicleOrCraft'
  | 'OtherConsumerCredit'
  | 'OtherLoan'
  | 'LoanForBusinessActivities'
  | 'GuaranteeReceivable';

export interface CreditExtractRequest {
  personalIdCode: string;
  extractPurpose: CreditRegisterExtractPurpose;
}

export interface FinancingRequestPayload {
  personIdCode: string;
  loanType: LoanType;
  loanPurposeOfUse: LoanPurposeOfUse;
}

export interface FinancingRequestResponse {
  id: string;
  personIdCode: string;
  loanType: LoanType;
  loanPurposeOfUse: LoanPurposeOfUse;
  submittedAt: string;
  banCheckStatus: 'PENDING' | 'CHECKED' | 'FAILED';
  voluntaryBanInEffect: boolean | null;
}

export interface CreditExtractResponse {
  extract: PcrCreditRegisterExtractResponse | null;
  historical: boolean;
}

export interface PcrCreditRegisterExtractResponse {
  statusMessage?: string | null;
  creditRegisterExtract?: CreditRegisterExtract | null;
}

export interface CreditRegisterExtract {
  extractReference?: string | null;
  creationTimeUtc?: string | null;
  personRequested?: PersonRequested | null;
  owner?: Requester | null;
  voluntaryBanOnCredits?: VoluntaryBanOnCredits | null;
  creditInformationSummary?: CreditInformationSummary | null;
  loans?: Loan[] | null;
  incomeData?: IncomeData[] | null;
  deceasedPerson?: DeceasedPerson | null;
}

export interface PersonRequested {
  idCodeType?: IdCodeType | null;
  idCode?: string | null;
}

export interface Requester {
  idCodeType?: IdCodeType | null;
  idCode?: string | null;
  countryCode?: string | null;
}

export interface VoluntaryBanOnCredits {
  isInEffect?: boolean | null;
  reason?: string | null;
}

export interface CreditInformationSummary {
  lendersCount?: number | null;
  loanContractsCount?: number | null;
  guaranteedLoanContractsCount?: number | null;
  repaymentsPaidLastAmount?: CurrencyAmount[] | null;
  sumOfMonthlyLeasingInstalments?: CurrencyAmount[] | null;
}

export interface CurrencyAmount {
  currencyCode?: string | null;
  sum?: number | null;
}

export interface Loan {
  loanType?: string | null;
  contractDate?: string | null;
  isLoanWithCollateral?: boolean | null;
  collateralType?: string[] | null;
  borrowersCount?: number | null;
  currencyCode?: string | null;
  businessInformation?: BusinessInformation | null;
  defermentPeriods?: DefermentPeriod[] | null;
  paymentPlan?: PaymentPlan | null;
  lumpSumLoan?: LumpSumLoan | null;
  runningAccountLoan?: RunningAccountLoan | null;
  leasingContract?: LeasingContract | null;
  delayedAmount?: DelayedAmount[] | null;
}

export interface BusinessInformation {
  borrowerBusinessID?: string | null;
  borrowerBusinessName?: string | null;
}

export interface DefermentPeriod {
  startDate?: string | null;
  endDate?: string | null;
}

export interface PaymentPlan {
  isInDebtArrangement?: boolean | null;
  isInBusinessRestructuringProgram?: boolean | null;
  accuracyIsDenied?: boolean | null;
}

export interface LumpSumLoan {
  repaymentMethod?: string | null;
  purposeOfUse?: string | null;
  amountIssued?: number | null;
  amountPaid?: number | null;
  balance?: number | null;
  plannedFinalDueDate?: string | null;
  amortizationFrequency?: number | null;
}

export interface RunningAccountLoan {
  creditLimit?: number | null;
  balance?: number | null;
  balanceDate?: string | null;
}

export interface LeasingContract {
  contractPeriodStartDate?: string | null;
  transactionPrice?: number | null;
}

export interface DelayedAmount {
  delayedInstalment?: number | null;
  originalDueDate?: string | null;
  isForeclosed?: boolean | null;
}

export interface IncomeData {
  year?: number | null;
  months?: IncomeMonth[] | null;
}

export interface IncomeMonth {
  month?: number | null;
  wagesGrossAmount?: number | null;
  wagesNetAmount?: number | null;
  benefitsGrossAmount?: number | null;
  benefitsNetAmount?: number | null;
}

export interface DeceasedPerson {
  dateOfDeath?: string | null;
}

export interface CreditExtractHistoryItem {
  id: string;
  extractReference: string | null;
  fetchedAt: string | null;
  voluntaryBanInEffect: boolean | null;
}
