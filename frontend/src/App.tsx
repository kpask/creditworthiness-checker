import { useEffect, useState, type ReactNode } from 'react';
import './App.css';
import { fetchCreditExtract, fetchCreditExtractById, fetchCreditExtractHistory } from './api/credit';
import type {
  CreditExtractHistoryItem,
  CreditExtractRequest,
  CreditRegisterExtract,
  CreditRegisterExtractPurpose,
} from './types/credit';

const DEFAULT_REQUEST: CreditExtractRequest = {
  personalIdCode: 'TEST-001',
  extractPurpose: 'NewConsumerCredit',
};

const formatDateTime = (value?: string | null) => {
  if (!value) return 'Not provided';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat('en-GB', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'UTC',
  }).format(date);
};

const formatDate = (value?: string | null) => {
  if (!value) return 'Not provided';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat('en-GB', {
    dateStyle: 'medium',
  }).format(date);
};

const formatScalar = (value: unknown): string => {
  if (value === null || value === undefined || value === '') return 'Not provided';
  if (typeof value === 'boolean') return value ? 'Yes' : 'No';
  if (typeof value === 'number') return Number.isFinite(value) ? String(value) : 'Not provided';
  if (typeof value === 'string') {
    if (/^\d{4}-\d{2}-\d{2}T/.test(value)) return formatDateTime(value);
    if (/^\d{4}-\d{2}-\d{2}$/.test(value)) return formatDate(value);
    return value;
  }
  if (Array.isArray(value)) return value.length > 0 ? value.map((item) => formatScalar(item)).join(', ') : 'Not provided';
  return String(value);
};

const isCurrencyFieldKey = (key: string) =>
  /(amount|balance|sum|limit|price|instalment|issued|paid|gross|net|value)/i.test(key);

const isCurrencyAmountObject = (value: unknown): value is { currencyCode?: string | null; sum?: number | string | null } => {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return false;

  const record = value as Record<string, unknown>;
  return ('currencyCode' in record || 'sum' in record) && !('loanType' in record || 'year' in record || 'month' in record);
};

const formatCurrencyAmountSummary = (value: { currencyCode?: string | null; sum?: number | string | null } | null | undefined): string => {
  if (!value) return 'Not provided';

  const sumValue = value.sum;
  const currencyValue = value.currencyCode;
  const formattedSum = sumValue === null || sumValue === undefined || sumValue === '' ? 'Not provided' : formatScalar(sumValue);
  const formattedCurrency = currencyValue === null || currencyValue === undefined || currencyValue === '' ? '' : formatScalar(currencyValue);

  if (!formattedCurrency) return `Sum: ${formattedSum}`;
  return `Sum: ${formattedSum} ${formattedCurrency}`;
};

const isCurrencyAmountList = (value: unknown): value is Array<{ currencyCode?: string | null; sum?: number | string | null }> => {
  return Array.isArray(value) && value.length > 0 && value.every((item) => isCurrencyAmountObject(item));
};

const isDateRangeList = (value: unknown): value is Array<Record<string, unknown>> => {
  return Array.isArray(value) && value.length > 0 && value.every((item) => !!(item && typeof item === 'object' && ('startDate' in item || 'endDate' in item)));
};

const isDelayedAmountList = (value: unknown): value is Array<Record<string, unknown>> => {
  return Array.isArray(value) && value.length > 0 && value.every((item) => !!(item && typeof item === 'object' && ('delayedInstalment' in item || 'originalDueDate' in item || 'isForeclosed' in item)));
};

const formatCurrencyAwareValue = (key: string, value: unknown, currencyCode?: string | null): ReactNode => {
  if (!currencyCode || !isCurrencyFieldKey(key) || value === null || value === undefined || value === '') {
    return renderDataValue(value, currencyCode);
  }

  if (typeof value === 'number' || typeof value === 'string') {
    return `${formatScalar(value)} ${currencyCode}`;
  }

  if (Array.isArray(value)) {
    return value.length === 0 ? 'Not provided' : value.map((entry) => formatCurrencyAwareValue(key, entry, currencyCode)).join(', ');
  }

  if (typeof value === 'object') {
    return renderDataValue(value, currencyCode);
  }

  return renderDataValue(value, currencyCode);
};

const monthNames = [
  'January',
  'February',
  'March',
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'December',
];

const isMonthFieldKey = (key: string) => ['month', 'monthNumber', 'monthOfYear'].includes(key.toLowerCase());

const formatMonthLabel = (value: unknown): string | null => {
  if (typeof value === 'number' && Number.isInteger(value)) {
    const index = value - 1;
    if (index >= 0 && index < monthNames.length) return monthNames[index];
  }

  if (typeof value === 'string') {
    const trimmed = value.trim();
    const parsed = Number(trimmed);
    if (Number.isInteger(parsed)) {
      const index = parsed - 1;
      if (index >= 0 && index < monthNames.length) return monthNames[index];
    }
  }

  return null;
};

const resolveMonthValue = (value: unknown): unknown => {
  if (Array.isArray(value)) {
    return value.map((item) => formatMonthLabel(item) ?? item);
  }

  return formatMonthLabel(value) ?? value;
};

const getBanStatus = (value: boolean | null | undefined) => {
  if (value === true) return 'Active';
  return 'Inactive';
};

const humanizeLabel = (value: string) =>
  value
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (letter) => letter.toUpperCase());

const shouldSuppressTitleField = (key: string, record: Record<string, unknown>): boolean => {
  const title = getContextualTitle(record, 0);
  const normalizedKey = key.toLowerCase();
  const yearValue = record.year;
  const monthValue = formatMonthLabel(record.month ?? record.monthNumber ?? record.monthOfYear);

  const yearString = typeof yearValue === 'number' || typeof yearValue === 'string' ? String(yearValue) : null;
  const monthString = typeof monthValue === 'string' ? monthValue : null;

  if (['year'].includes(normalizedKey) && yearString) {
    return title === yearString || Boolean(monthString && title === `${monthString} ${yearString}`);
  }

  if (['month', 'monthnumber', 'monthofyear'].includes(normalizedKey) && monthString) {
    return title === monthString || Boolean(yearString && title === `${monthString} ${yearString}`);
  }

  return false;
};

const getContextualTitle = (value: unknown, fallbackIndex: number): string => {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    return `Item ${fallbackIndex + 1}`;
  }

  const record = value as Record<string, unknown>;
  const loanType = typeof record.loanType === 'string' ? record.loanType : '';
  const lenderName = typeof record.lenderName === 'string' ? record.lenderName : '';
  const borrowerName = typeof record.borrowerBusinessName === 'string' ? record.borrowerBusinessName : '';
  const businessName = typeof record.businessName === 'string' ? record.businessName : '';
  const startDate = typeof record.startDate === 'string' ? record.startDate : null;
  const endDate = typeof record.endDate === 'string' ? record.endDate : null;
  const year = typeof record.year === 'number' ? record.year : null;
  const monthValue = formatMonthLabel(record.month ?? record.monthNumber ?? record.monthOfYear);
  const currencyCode = typeof record.currencyCode === 'string' ? record.currencyCode : null;
  const sumValue = typeof record.sum === 'number' || typeof record.sum === 'string' ? record.sum : undefined;

  if (startDate || endDate) return `${formatDate(startDate ?? endDate ?? undefined)} - ${formatDate(endDate ?? startDate ?? undefined)}`;
  if (currencyCode && sumValue !== undefined) return `${formatScalar(sumValue)} ${currencyCode}`;
  if (monthValue && year) return `${monthValue} ${year}`;
  if (loanType && (lenderName || borrowerName || businessName)) return `${loanType} · ${lenderName || borrowerName || businessName}`;
  if (loanType) return loanType;
  if (lenderName) return lenderName;
  if (borrowerName) return borrowerName;
  if (businessName) return businessName;
  if (monthValue) return monthValue;
  if (year) return String(year);

  const yearValue = typeof record.year === 'string' ? Number(record.year) : null;
  if (yearValue && Number.isFinite(yearValue)) return String(yearValue);

  return `Item ${fallbackIndex + 1}`;
};

const orderDisplayEntries = (entries: [string, unknown][]) => {
  const priority: Record<string, number> = {
    runningAccountLoan: 10,
    leasingContract: 11,
    delayedAmount: 12,
    months: 20,
  };

  return [...entries].sort(([aKey], [bKey]) => {
    const aPriority = priority[aKey] ?? 0;
    const bPriority = priority[bKey] ?? 0;
    return aPriority - bPriority;
  });
};

const formatInlinePairs = (value: Record<string, unknown>, inheritedCurrencyCode?: string | null): ReactNode => {
  const currencyCode = typeof value.currencyCode === 'string' ? value.currencyCode : inheritedCurrencyCode ?? null;

  return (
    <div className="inline-pairs">
      {orderDisplayEntries(Object.entries(value))
      .filter(
        ([key, child]) =>
          key !== 'currencyCode' &&
          key !== 'months' &&
          !shouldSuppressTitleField(key, value) &&
          hasMeaningfulContent(child),
      )
      .map(([key, child]) => {
        const resolvedValue = isMonthFieldKey(key) ? resolveMonthValue(child) : child;
        const isBooleanLike = typeof resolvedValue === 'boolean';
        return (
          <div key={key} className="inline-pair">
            <span className="inline-key">{humanizeLabel(key)}:</span>
            <span className="inline-value">{isBooleanLike ? formatCurrencyAwareValue(key, resolvedValue, currencyCode) : formatCurrencyAwareValue(key, resolvedValue, currencyCode)}</span>
          </div>
        );
      })}
  </div>
);
};

const renderDataValue = (value: unknown, inheritedCurrencyCode?: string | null, contextKey?: string): ReactNode => {
  if (value === null || value === undefined || value === '') return 'Not provided';

  if (Array.isArray(value)) {
    if (value.length === 0) return null;

    const listLabel = contextKey
      ? humanizeLabel(contextKey)
      : isDelayedAmountList(value)
        ? 'Delayed Amount'
        : isDateRangeList(value)
          ? 'Deferment Periods'
          : isCurrencyAmountList(value)
            ? 'Currency Amount'
            : null;

    if (listLabel && (isCurrencyAmountList(value) || isDateRangeList(value) || isDelayedAmountList(value))) {
      return (
        <details className="nested-item" open={false}>
          <summary className="nested-summary">{listLabel}</summary>
          <div className="nested-body">
            {value.map((item, index) => {
              if (isCurrencyAmountList(value)) {
                const currency = String((item as Record<string, unknown>).currencyCode ?? 'Unknown');
                const amount = formatScalar((item as Record<string, unknown>).sum ?? 'Not provided');
                return (
                  <div key={`${currency}-${amount}-${index}`} className="inline-pair">
                    <span className="inline-key">{`${currency}:`}</span>
                    <span className="inline-value">{amount}</span>
                  </div>
                );
              }

              if (isDateRangeList(value)) {
                const startDate = formatDate((item as Record<string, unknown>).startDate as string | null | undefined);
                const endDate = formatDate((item as Record<string, unknown>).endDate as string | null | undefined);
                return (
                  <div key={`${startDate}-${endDate}-${index}`} className="inline-pair">
                    <span className="inline-value">{`${startDate} - ${endDate}`}</span>
                  </div>
                );
              }

              const delayedInstalment = formatScalar((item as Record<string, unknown>).delayedInstalment ?? 'Not provided');
              const originalDueDate = formatDate((item as Record<string, unknown>).originalDueDate as string | null | undefined);
              const isForeclosed = (item as Record<string, unknown>).isForeclosed;
              const status = isForeclosed === true ? 'Foreclosed' : isForeclosed === false ? 'Not foreclosed' : 'Foreclosure unknown';
              return (
                <div key={`${delayedInstalment}-${originalDueDate}-${status}-${index}`} className="inline-pair">
                  <span className="inline-value">{`${delayedInstalment} — ${originalDueDate} — ${status}`}</span>
                </div>
              );
            })}
          </div>
        </details>
      );
    }

    if (value.every((item) => item === null || item === undefined || typeof item !== 'object')) {
      return <span>{value.map((item) => formatScalar(item)).join(', ')}</span>;
    }

    return (
      <div className="nested-list">
        {value.map((item, index) => {
          const title = getContextualTitle(item, index);
          const record = item && typeof item === 'object' && !Array.isArray(item) ? (item as Record<string, unknown>) : null;
          const isDefermentPeriod = !!(record && ('startDate' in record || 'endDate' in record));
          const isCurrencyAmount = isCurrencyAmountObject(item);
          const isDelayedAmountObject = !!(record && ('delayedInstalment' in record || 'originalDueDate' in record || 'isForeclosed' in record));
          const isYearMonthGroup = !!(record && 'months' in record && Array.isArray(record.months));
          const isLoanCollectionItem = !!(record && ('loanType' in record || 'currencyCode' in record || 'borrowersCount' in record || 'businessInformation' in record || 'lumpSumLoan' in record || 'runningAccountLoan' in record || 'leasingContract' in record));
          const summaryTitle = isYearMonthGroup
            ? String(record?.year ?? title)
            : isLoanCollectionItem
              ? `Loan ${index + 1}`
              : isCurrencyAmount
                ? formatCurrencyAmountSummary(item as { currencyCode?: string | null; sum?: number | string | null })
                : isDefermentPeriod
                  ? `${formatDate((record as Record<string, unknown>).startDate as string | null | undefined)} - ${formatDate((record as Record<string, unknown>).endDate as string | null | undefined)}`
                  : isDelayedAmountObject
                    ? `${formatScalar((record as Record<string, unknown>).delayedInstalment ?? 'Not provided')} — ${formatDate((record as Record<string, unknown>).originalDueDate as string | null | undefined)}${((record as Record<string, unknown>).isForeclosed === true) ? ' — Foreclosed' : ''}`
                    : title;

          const shouldRenderBody = isYearMonthGroup || isLoanCollectionItem || (!isDefermentPeriod && !isCurrencyAmount && !isDelayedAmountObject);

          return (
            <details key={`${title}-${index}`} className="nested-item" open={false}>
              <summary className="nested-summary">{summaryTitle}</summary>
              {shouldRenderBody && (
                <div className="nested-body">
                  {isYearMonthGroup ? renderDataValue(record?.months, inheritedCurrencyCode) : renderDataValue(item, inheritedCurrencyCode)}
                </div>
              )}
            </details>
          );
        })}
      </div>
    );
  }

  if (typeof value === 'object') {
    if (isCurrencyAmountObject(value)) {
      return <span>{formatCurrencyAmountSummary(value)}</span>;
    }
    const record = value as Record<string, unknown>;
    const entries = orderDisplayEntries(Object.entries(record));
    if (entries.length === 0) return 'Not provided';

    const currencyCode = typeof record.currencyCode === 'string' ? record.currencyCode : inheritedCurrencyCode ?? null;

    if ('months' in record && Array.isArray(record.months)) {
      const yearValue = typeof record.year === 'number' || typeof record.year === 'string' ? String(record.year) : 'Months';
      return (
        <details className="nested-item" open={false}>
          <summary className="nested-summary">{yearValue}</summary>
          <div className="nested-body">{renderDataValue(record.months, currencyCode)}</div>
        </details>
      );
    }

    const isSimpleInlineObject = entries.length <= 4 && entries.every(([key, child]) => {
      if (key === 'currencyCode' || key === 'months') return true;
      if (child === null || child === undefined || child === '') return true;
      if (typeof child === 'string' || typeof child === 'number' || typeof child === 'boolean') return true;
      if (Array.isArray(child)) return child.every((entry) => typeof entry !== 'object');
      return false;
    });

    if (isSimpleInlineObject) {
      return formatInlinePairs(record, currencyCode);
    }

    return (
      <div className="nested-object">
        {entries
          .filter(([key, child]) => {
            if (key === 'currencyCode' || key === 'months') return false;
            if (shouldSuppressTitleField(key, record)) return false;
            return hasMeaningfulContent(child);
          })
          .map(([key, child]) => {
            const resolvedValue = isMonthFieldKey(key) ? resolveMonthValue(child) : child;
            const isBooleanInline = typeof resolvedValue === 'boolean';
            const isStandaloneList = Array.isArray(resolvedValue) && (isCurrencyAmountList(resolvedValue) || isDateRangeList(resolvedValue) || isDelayedAmountList(resolvedValue));
            const fieldValue = (Array.isArray(resolvedValue) || (resolvedValue !== null && typeof resolvedValue === 'object'))
              ? renderDataValue(resolvedValue, currencyCode, key)
              : formatCurrencyAwareValue(key, resolvedValue, currencyCode);
            return (
              <div key={key} className="field-row">
                {!isStandaloneList && <div className="field-key">{humanizeLabel(key)}:</div>}
                <div className="field-value">
                  {isBooleanInline ? `${fieldValue}` : fieldValue}
                </div>
              </div>
            );
          })}
      </div>
    );
  }

  if (typeof value === 'boolean') return value ? 'Yes' : 'No';
  if (typeof value === 'number') return String(value);
  if (typeof value === 'string') return formatScalar(value);

  return String(value);
};

const hasMeaningfulContent = (value: unknown): boolean => {
  if (value === null || value === undefined || value === '') return false;

  if (Array.isArray(value)) {
    return value.length > 0 && value.some((entry) => hasMeaningfulContent(entry));
  }

  if (typeof value === 'object') {
    return Object.values(value as Record<string, unknown>).some((entry) => hasMeaningfulContent(entry));
  }

  return true;
};

const getDisplayId = (extract?: CreditRegisterExtract | null) => {
  return extract?.personRequested?.idCode ?? extract?.owner?.idCode ?? 'Unknown consumer';
};

function App() {
  const [request, setRequest] = useState<CreditExtractRequest>(DEFAULT_REQUEST);
  const [historyLookup, setHistoryLookup] = useState(DEFAULT_REQUEST.personalIdCode);
  const [history, setHistory] = useState<CreditExtractHistoryItem[]>([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [fetchLoading, setFetchLoading] = useState(false);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [detailsError, setDetailsError] = useState<string | null>(null);
  const [detailsExtract, setDetailsExtract] = useState<CreditRegisterExtract | null>(null);
  const [loadingItemId, setLoadingItemId] = useState<string | null>(null);
  const [loadedHistoryConsumer, setLoadedHistoryConsumer] = useState<string | null>(null);
  const [copyState, setCopyState] = useState<'idle' | 'copied' | 'error'>('idle');

  const loadHistory = async (personIdCode: string) => {
    const trimmedId = personIdCode.trim();
    if (!trimmedId) {
      setHistoryError('Please enter a consumer ID or test ID.');
      return;
    }

    setHistoryLoading(true);
    setHistoryError(null);

    try {
      const nextHistory = await fetchCreditExtractHistory(trimmedId);
      setHistory(nextHistory ?? []);
      setLoadedHistoryConsumer(trimmedId);
    } catch (loadError) {
      const message = loadError instanceof Error ? loadError.message : 'Unable to load history.';
      setHistoryError(message);
      setHistory([]);
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleFetchData = async () => {
    const trimmedId = request.personalIdCode.trim();
    const payload = {
      ...request,
      personalIdCode: trimmedId,
    };

    if (!trimmedId) {
      setFetchError('Please enter a consumer ID or test ID.');
      return;
    }

    setFetchLoading(true);
    setFetchError(null);

    try {
      const result = await fetchCreditExtract(payload);
      const extracted = result?.extract?.creditRegisterExtract ?? null;

      if (!extracted) {
        setFetchError('The backend returned an empty extract response.');
        return;
      }

      setDetailsExtract(extracted);
      setDetailsError(null);
      setDetailsOpen(true);
      setHistoryLookup(trimmedId);

      if (loadedHistoryConsumer === trimmedId) {
        await loadHistory(trimmedId);
      }
    } catch (fetchError) {
      const message = fetchError instanceof Error ? fetchError.message : 'Unable to fetch credit extract.';
      setFetchError(message);
    } finally {
      setFetchLoading(false);
    }
  };

  const handleHistoryLookup = async () => {
    const trimmedId = historyLookup.trim();
    if (!trimmedId) {
      setHistoryError('Please enter a consumer ID or test ID.');
      return;
    }

    setHistoryLookup(trimmedId);
    await loadHistory(trimmedId);
  };

  const handleViewDetails = async (item: CreditExtractHistoryItem) => {
    if (!item.id) return;

    setDetailsOpen(true);
    setDetailsLoading(true);
    setDetailsError(null);
    setLoadingItemId(item.id);

    try {
      const result = await fetchCreditExtractById(item.id);
      const extracted = result?.extract?.creditRegisterExtract ?? null;
      setDetailsExtract(extracted);
      if (!extracted) {
        setDetailsError('The selected extract could not be loaded.');
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to load the selected extract.';
      setDetailsError(message);
      setDetailsExtract(null);
    } finally {
      setDetailsLoading(false);
      setLoadingItemId(null);
    }
  };

  const handleCopyRawJson = async () => {
    if (!detailsExtract) return;

    const payload = JSON.stringify(detailsExtract, null, 2);

    try {
      await navigator.clipboard.writeText(payload);
      setCopyState('copied');
      window.setTimeout(() => setCopyState('idle'), 1500);
    } catch {
      setCopyState('error');
      window.setTimeout(() => setCopyState('idle'), 1500);
    }
  };

  const handleDownloadRawJson = () => {
    if (!detailsExtract) return;

    const payload = JSON.stringify(detailsExtract, null, 2);
    const blob = new Blob([payload], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${getDisplayId(detailsExtract).replace(/\s+/g, '-').toLowerCase() || 'credit-extract'}.json`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  };

  useEffect(() => {
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setDetailsOpen(false);
      }
    };

    window.addEventListener('keydown', closeOnEscape);
    return () => window.removeEventListener('keydown', closeOnEscape);
  }, []);

  return (
    <div className="dashboard-shell">
      <header className="topbar">
        <div>
          <h1>Credit Register</h1>
          <p className="subtitle">Consumer credit assessment</p>
        </div>
      </header>

      <main className="content-grid">
        <section className="panel fetch-panel">
          <div className="panel-heading">
            <h2>Fetch Credit Extract</h2>
          </div>

          <div className="fetch-controls fetch-row">
            <div className="fetch-field">
              <label className="field-label" htmlFor="consumer-id">
                Consumer ID / test ID
              </label>
              <input
                id="consumer-id"
                type="text"
                value={request.personalIdCode}
                onChange={(event) =>
                  setRequest((current) => ({
                    ...current,
                    personalIdCode: event.target.value,
                  }))
                }
                placeholder="TEST-001"
              />
            </div>

            <div className="fetch-field">
              <label className="field-label" htmlFor="extract-purpose">
                Purpose
              </label>
              <select
                id="extract-purpose"
                value={request.extractPurpose}
                onChange={(event) =>
                  setRequest((current) => ({
                    ...current,
                    extractPurpose: event.target.value as CreditRegisterExtractPurpose,
                  }))
                }
              >
                <option value="NewConsumerCredit">New Consumer Credit</option>
                <option value="IncreaseOfConsumerCreditPrincipalOrCreditLimit">Increase Consumer Credit</option>
                <option value="ChangesToTermsOfConsumerCredit">Changes to Terms</option>
                <option value="GuaranteeOrThirdPartyPledgeForConsumerCredit">Guarantee / Pledge</option>
                <option value="NewLoan">New Loan</option>
                <option value="IncreaseOfLoanPrincipalOrCreditLimit">Increase Loan Principal</option>
                <option value="ChangesToTerms">Changes to Loan Terms</option>
                <option value="GuaranteeOrThirdPartyPledge">Loan Guarantee / Pledge</option>
              </select>
            </div>

            <div className="fetch-action-wrap">
              <button type="button" className="fetch-button" onClick={() => void handleFetchData()} disabled={fetchLoading}>
                {fetchLoading ? 'Fetching…' : 'Fetch'}
              </button>
            </div>
          </div>

          {fetchError && <div className="message error">{fetchError}</div>}
        </section>

        <section className="panel history-panel">
          <div className="panel-heading">
            <h2>Extract History</h2>
          </div>

          <div className="history-lookup-row">
            <input
              type="text"
              value={historyLookup}
              onChange={(event) => setHistoryLookup(event.target.value)}
              placeholder="TEST-001"
              className="history-search-input"
            />
            <button type="button" className="history-search-button" onClick={() => void handleHistoryLookup()} disabled={historyLoading}>
              {historyLoading ? 'Loading…' : 'Search'}
            </button>
          </div>

          {historyLoading && <div className="loading-state">Loading history…</div>}
          {!historyLoading && historyError && <div className="message error">{historyError}</div>}
          {!historyLoading && !historyError && history.length === 0 && (
            <div className="empty-state">No prior extracts found for this consumer.</div>
          )}

          {!historyLoading && history.length > 0 && (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Fetch time</th>
                    <th>Extract ref.</th>
                    <th>Voluntary ban</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((item) => {
                    const status = getBanStatus(item.voluntaryBanInEffect);

                    return (
                      <tr key={item.id} className="history-row" onClick={() => void handleViewDetails(item)}>
                        <td>{formatDateTime(item.fetchedAt)}</td>
                        <td>{item.extractReference ?? 'Not provided'}</td>
                        <td className="status-text">{status}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>

      {detailsOpen && (
        <div className="modal-backdrop" onClick={() => setDetailsOpen(false)}>
          <div className="modal-dialog" onClick={(event) => event.stopPropagation()} role="dialog" aria-modal="true">
            <div className="modal-header">
              <div className="modal-header-title">
                {detailsExtract ? getDisplayId(detailsExtract) : 'Extract details'}
              </div>
              <button type="button" className="close-button" onClick={() => setDetailsOpen(false)} aria-label="Close modal">
                ×
              </button>
            </div>

            {detailsLoading && <div className="loading-state">Loading extract details…</div>}
            {detailsError && <div className="message error">{detailsError}</div>}

            {!detailsLoading && detailsExtract && (
              <div className="modal-body">
                <div className="detail-summary">
                  <div className="detail-item inline-summary-item">
                    <span className="meta-label">Consumer ID</span>
                    <strong>{getDisplayId(detailsExtract)}</strong>
                  </div>
                  <div className="detail-item inline-summary-item">
                    <span className="meta-label">Extract reference</span>
                    <strong>{detailsExtract.extractReference ?? 'Not provided'}</strong>
                  </div>
                  <div className="detail-item inline-summary-item">
                    <span className="meta-label">Creation time</span>
                    <strong>{formatDateTime(detailsExtract.creationTimeUtc)}</strong>
                  </div>
                  <div className="detail-item inline-summary-item">
                    <span className="meta-label">Voluntary credit ban</span>
                    <strong>{getBanStatus(detailsExtract.voluntaryBanOnCredits?.isInEffect)}</strong>
                  </div>
                </div>

                <div className="data-sections">
                  {Object.entries(detailsExtract)
                    .filter(
                      ([key, value]) =>
                        !['statusMessage', 'personRequested', 'owner', 'extractReference', 'creationTimeUtc'].includes(key) &&
                        hasMeaningfulContent(value),
                    )
                    .map(([key, value]) => (
                      <details key={key} className="data-section">
                        <summary>{humanizeLabel(key)}</summary>
                        <div className="data-section-body">{renderDataValue(value)}</div>
                      </details>
                    ))}
                </div>

                <details className="data-section raw-json-section">
                  <summary>
                    <span>Raw JSON</span>
                    <span className="raw-json-actions">
                      <button type="button" className="raw-json-action" onClick={(event) => {
                        event.stopPropagation();
                        void handleCopyRawJson();
                      }}>
                        {copyState === 'copied' ? 'Copied' : copyState === 'error' ? 'Copy failed' : 'Copy'}
                      </button>
                      <button type="button" className="raw-json-action" onClick={(event) => {
                        event.stopPropagation();
                        handleDownloadRawJson();
                      }}>
                        Download
                      </button>
                    </span>
                  </summary>
                  <pre className="raw-json">{JSON.stringify(detailsExtract, null, 2)}</pre>
                </details>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
