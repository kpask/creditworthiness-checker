import type {
  CreditExtractHistoryItem,
  CreditExtractRequest,
  CreditExtractResponse,
} from '../types/credit';

const API_BASE = 'http://localhost:8080';

async function parseJson<T>(response: Response): Promise<T> {
  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return undefined as T;
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const payload = await parseJson<{ message?: string; error?: string; detail?: string }>(response);
      message = payload?.message ?? payload?.error ?? payload?.detail ?? message;
    } catch {
      // Ignore JSON parse issues and fall back to the default message.
    }
    throw new Error(message);
  }

  return parseJson<T>(response);
}

export async function fetchCreditExtract(request: CreditExtractRequest) {
  const response = await fetch(`${API_BASE}/fetch`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  return handleResponse<CreditExtractResponse>(response);
}

export async function fetchCreditExtractHistory(personIdCode: string) {
  const response = await fetch(`${API_BASE}/fetch/${encodeURIComponent(personIdCode)}/history`, {
    method: 'GET',
  });

  return handleResponse<CreditExtractHistoryItem[]>(response);
}

export async function fetchCreditExtractById(creditExtractId: string) {
  const response = await fetch(`${API_BASE}/fetch/${encodeURIComponent(creditExtractId)}`, {
    method: 'GET',
  });

  return handleResponse<CreditExtractResponse>(response);
}
