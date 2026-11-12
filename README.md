# Java Transaction Processing System

## 1. Project Overview

This project implements a Java-based transaction processing system that handles financial transactions with validation, duplicate detection, idempotency, retry handling, and out-of-order transaction processing.

The application supports CREDIT and DEBIT transactions and maintains account balances using an in-memory repository.

## 2. Technologies Used

- Java 18
- Maven
- JUnit 5
- Jackson (JSON processing)
- IntelliJ IDEA

## 3. Key Features

- Transaction validation for mandatory fields and business rules.
- CREDIT and DEBIT transaction processing.
- Account balance management.
- Idempotency key validation to prevent duplicate processing.
- Duplicate transaction ID detection.
- Out-of-order transaction handling using sequence numbers.
- Pending transaction management.
- Retry mechanism for temporary processing failures.
- Immediate failure handling for non-retryable errors.
- JSON input and output generation.
- Unit and functional test coverage.

## 4. Project Structure

```text
TransactionProcessingAssessment
│
├── src
│   ├── main
│   │   └── java
│   │       └── com.assessment
│   │           ├── TransactionApplication.java
│   │           ├── model
│   │           ├── service
│   │           ├── repository
│   │           ├── validator
│   │           ├── retry
│   │           ├── exception
│   │           └── input
│   │
│   └── test
│       └── java
│           └── com.assessment
│               ├── unit
│               └── functional
│
├── docs
│   └── QA-Test-Cases.md
│
├── sample-data
│   ├── input.json
│   ├── expected-output.json
│   └── output.json
│
├── pom.xml
└── README.md
```

## 5. Transaction Processing Flow

1. Read transaction details from the input JSON file.
2. Validate the transaction fields.
3. Check for duplicate transaction IDs and idempotency keys.
4. Verify the transaction sequence number.
5. Store out-of-order transactions as pending.
6. Process transactions in the correct sequence.
7. Apply CREDIT or DEBIT operations to the account balance.
8. Retry temporary processing failures up to the configured maximum attempts.
9. Update transaction status and sequence information.
10. Process pending transactions when earlier transactions arrive.
11. Generate the transaction processing summary as JSON.

## 6. Transaction Statuses

| Status | Description |
|---|---|
| RECEIVED | Transaction received or waiting for an earlier sequence |
| PROCESSING | Transaction currently being processed |
| PROCESSED | Transaction successfully completed |
| DUPLICATE | Duplicate transaction or idempotency key detected |
| RETRY_PENDING | Transaction waiting for another retry attempt |
| FAILED | Transaction failed validation or processing |

## 7. How to Run the Application

### Prerequisites

- Java Development Kit 18
- IntelliJ IDEA
- Maven support

### Execution

1. Clone or download the project.
2. Open the project in IntelliJ IDEA.
3. Allow Maven dependencies to load.
4. Place the input transactions in `sample-data/input.json`.
5. Open `TransactionApplication.java`.
6. Run the main method.

The application processes the transactions and generates:

`sample-data/output.json`

## 8. Running Tests

The project uses JUnit 5.

To execute the complete test suite using Maven:

```bash
mvn clean test
```

Alternatively, execute the test classes directly from IntelliJ IDEA.

The functional test suite includes 12 scenarios covering:

- Successful credit and debit workflows.
- Out-of-order transaction processing.
- Duplicate transaction detection.
- Invalid transaction handling.
- Retry exhaustion.
- Idempotency key validation.
- Duplicate pending transaction handling.
- Zero amount validation.
- Invalid currency validation.
- Insufficient balance handling.
- Multiple pending transactions processed in sequence.

## 9. Design Decisions and Assumptions

- Transaction data and account balances are maintained in memory.
- No external database or user interface is required.
- Sequence numbers are maintained independently for each account.
- The expected initial sequence number is 1.
- Only transactions with the expected sequence number are processed.
- Transactions with future sequence numbers remain pending.
- The maximum retry attempts are configured as 3.
- Only retryable processing exceptions trigger another attempt.
- Non-retryable failures are marked as FAILED.
- The sequence number advances after a processing attempt reaches a terminal result, including failure.
- Idempotency keys are reserved when a valid transaction is accepted, including while it is pending. A failed accepted transaction does not release its key.

## 10. Current Implementation Limitations

- The repository uses in-memory collections; data is not persisted after application shutdown.
- The current implementation is designed for single-threaded processing and does not provide concurrent transaction safety.
- Account balances are maintained by account ID, assuming a single currency per account for this assessment.
- Retry failures are simulated for testing purposes.

## 11. Expected Result

The application should process valid transactions in the correct sequence, prevent duplicate processing, maintain accurate account balances, handle temporary failures through retries, and generate a JSON processing summary.
