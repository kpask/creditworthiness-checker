# Positive Credit Register Simulator

A Spring Boot and React application that simulates integration with Finland’s Positive Credit Register (PCR). It allows users to request credit register extracts for individuals, store successful responses, review fetch history, and inspect previously retrieved extracts.

The application uses a mock PCR API for local development and testing.

## Features

- Request a credit register extract for an individual using their personal identity code.
- Specify the purpose of the extract request.
- Display credit information returned by the PCR.
- Store successful extracts in PostgreSQL.
- View an individual's fetch history and retrieve previously stored extracts.
- Fall back to the latest historical extract when the PCR is temporarily unavailable.
- Monitor financing requests involving individuals with an active voluntary credit ban and prepare scheduled reports.

## Technology Stack

- **Backend:** Java 17, Spring Boot, Maven
- **Frontend:** React, TypeScript, Vite
- **Database:** PostgreSQL, Spring Data JPA
- **External integration:** REST client with a local mock PCR API

## API

### Request a credit register extract

`POST /fetch`

Example request:

```json
{
  "personalIdCode": "TEST-001",
  "extractPurpose": "NewConsumerCredit"
}
```

- `personalIdCode` identifies the individual whose credit information is requested.
- `extractPurpose` specifies the reason for requesting the extract.

The application focuses on individual consumers, so the identifier type is not specified by the client. The backend uses the appropriate personal identity code type when constructing the PCR request.

Available extract purposes are defined by the backend's `CreditRegisterExtractPurpose` enum. Consumer-related purposes include:

- `NewConsumerCredit`
- `IncreaseOfConsumerCreditPrincipalOrCreditLimit`
- `ChangesToTermsOfConsumerCredit`
- `GuaranteeOrThirdPartyPledgeForConsumerCredit`

The enum also contains additional purposes relating to loans and guarantees. The frontend does not currently retrieve purposes through a separate lookup endpoint.

### Retrieve an individual's fetch history

`GET /fetch/{personIdCode}/history`

Returns stored extracts for the specified individual, ordered from newest to oldest.

### Retrieve a stored extract

`GET /fetch/{creditExtractId}`

Returns the stored extract identified by its UUID.

## Error Handling

The backend validates requests and handles PCR failures. When the PCR is unavailable, it attempts to return the latest stored extract for the requested individual.

Historical fallback is possible only if a suitable stored extract exists.

Typical HTTP status codes:

- `400 Bad Request` — invalid request or validation failure.
- `404 Not Found` — requested extract does not exist.
- `502 Bad Gateway` or `503 Service Unavailable` — PCR request failed or the service is unavailable.

## Configuration

Configure the application using environment variables or the corresponding Spring configuration properties.

| Variable | Description |
|---|---|
| `PCR_REQUESTER_ID` | Identifier of the requesting institution |
| `PCR_REQUESTER_COUNTRY` | Country of the requesting institution |
| `PCR_BASE_URL` | Base URL of the PCR API |
| `PCR_CONNECT_TIMEOUT_MS` | PCR connection timeout in milliseconds |
| `PCR_READ_TIMEOUT_MS` | PCR response timeout in milliseconds |
| `DB_NAME` | PostgreSQL database name |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `MOCK_PCR_AVAILABLE` | Controls mock PCR availability for testing failure handling |

Keep this list aligned with the variables actually supported by your application configuration.

## Running Locally

### Prerequisites

- Java 17
- PostgreSQL
- Node.js and npm

### Backend

1. Create a PostgreSQL database and configure the database connection.
2. Set the required PCR and database configuration values.
3. Start the backend:

```bash
./mvnw spring-boot:run
```

### Frontend

From the frontend directory:

```bash
npm install
npm run dev
```

Configure the frontend API base URL to point to the running backend.

## Mock PCR API

The project includes a mock PCR endpoint for local development and testing without access to the actual Finnish Positive Credit Register.

The mock supports testing successful extract retrieval and failure-handling scenarios. Identifiers such as `TEST-001` are synthetic test values, not real Finnish personal identity codes.

## Monitoring

The application supports monitoring financing requests involving individuals with an active voluntary credit ban and preparing reports for delivery to `pcr_monitoring@dansketest.dk`.

The monitoring schedule and email delivery configuration depend on the application's implementation.

## Limitations

- PCR integration is simulated; the application does not connect to the production register.
- Authentication, user management, and production-grade access control are outside the current scope.
- Sample identifiers and mock responses are intended for development and testing.
- This is a proof of concept, not a production-ready financial system.