# Transaction Processing System – Design Document

## 1. Architecture Overview

The application follows a layered design to separate transaction validation, business processing, data storage, retry handling, and JSON input/output operations.

### Main Components

| Component | Responsibility |
|---|---|
| TransactionApplication | Application entry point and orchestration |
| Transaction | Represents transaction information |
| TransactionValidator | Validates transaction fields and business rules |
| TransactionProcessingService | Coordinates transaction processing |
| TransactionRepository | Maintains transactions, balances, sequences and pending transactions |
| RetryHandler | Handles temporary processing failures |
| TransactionJsonReader | Reads transactions from JSON |
| TransactionJsonWriter | Generates JSON processing results |

## 2. Architecture Flow

```text
Input JSON
    |
    v
TransactionJsonReader
    |
    v
TransactionApplication
    |
    v
TransactionProcessingService
    |
    +----> TransactionValidator
    |
    +----> Duplicate / Idempotency Check
    |
    +----> Sequence Validation
    |
    +----> Pending Transaction Management
    |
    +----> RetryHandler
    |
    v
TransactionRepository
    |
    v
Account Balance and Transaction Status
    |
    v
TransactionJsonWriter
    |
    v
Output JSON
```

## 3. Transaction Validation

Each transaction is validated before processing.

Validation rules include:

- Transaction ID must not be empty.
- Account ID must not be empty.
- Transaction type must be CREDIT or DEBIT.
- Amount must be greater than zero.
- Currency must contain a valid three-letter code.
- Sequence number must be greater than zero.
- Idempotency key must be provided.

Invalid transactions are marked as FAILED.

## 4. Idempotency and Duplicate Handling

The system uses two checks:

### Transaction ID

A transaction ID identifies a transaction. If the same transaction ID is received again, it is treated as a duplicate.

### Idempotency Key

An idempotency key prevents the same request from being processed more than once, even when it arrives with a different transaction ID.

The repository maintains:

- Processed idempotency keys.
- Reserved idempotency keys for accepted transactions awaiting completion.

## 5. Sequence Management

Transactions are processed in sequence for each account.

Example:

| Arrival order | Transaction sequence | Action |
|---|---:|---|
| First | 3 | Stored as pending |
| Second | 2 | Stored as pending |
| Third | 1 | Processed |
| Next | 2 | Processed from pending queue |
| Next | 3 | Processed from pending queue |

A TreeMap is used to organize pending transactions by sequence number.

## 6. Retry Mechanism

The retry handler distinguishes between temporary and permanent failures.

- RetryableProcessingException: The transaction is attempted again.
- NonRetryableProcessingException: The transaction is immediately marked as FAILED.
- Maximum retry attempts: 3.
- If all attempts fail, the transaction is marked as FAILED.

## 7. Account Balance Processing

### CREDIT

Updated balance = Current balance + Transaction amount.

### DEBIT

Updated balance = Current balance - Transaction amount.

A debit transaction is rejected when the available balance is insufficient.

BigDecimal is used for monetary calculations to avoid floating-point precision problems.

## 8. Data Storage

The repository uses in-memory Java collections:

- HashMap for transactions and account balances.
- HashSet for processed and reserved idempotency