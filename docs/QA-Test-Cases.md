# Transaction Processing - QA Test Cases

## 1. Functional Test Cases

| Test ID | Scenario | Test Data | Expected Result |
|---|---|---|---|
| QA-001 | Process credit transaction | Credit 1000 INR | Balance increases to 1000 |
| QA-002 | Process debit transaction | Debit 300 INR from balance 1000 | Balance decreases to 700 |
| QA-003 | Insufficient balance | Debit 500 INR from zero balance | Transaction FAILED; balance unchanged |
| QA-004 | Duplicate transaction ID | Submit the same transaction ID twice | Duplicate transaction rejected; balance not updated twice |
| QA-005 | Out-of-order transaction | Submit sequence 2 before sequence 1 | Transactions processed in correct sequence |
| QA-006 | Invalid amount | Negative transaction amount | Transaction rejected; balance unchanged |
| QA-007 | Temporary processing failure | Fail once, then succeed | Transaction succeeds on retry |
| QA-008 | Retry exhaustion | Fail all three processing attempts | Transaction FAILED after maximum retry attempts |
| QA-009 | Invalid currency | Currency with invalid format | Transaction rejected |
| QA-010 | Missing account ID | Empty account ID | Transaction rejected |
| QA-011 | Zero amount transaction | Amount = 0 | Transaction rejected; balance unchanged |
| QA-012 | Debit exceeding available balance | Available balance = 100; debit = 500 | Transaction FAILED; balance unchanged |
| QA-013 | Multiple pending transactions | Submit sequence 3, then 2, then 1 | Pending transactions processed in sequence 1, 2, 3 |
| QA-014 | Duplicate idempotency key | Different transaction IDs with the same idempotency key | Second transaction rejected; balance not updated twice |

## 2. Regression Test Coverage

The following areas must be revalidated whenever transaction-processing logic changes:

- Credit and debit calculations
- Account balance accuracy
- Transaction sequence handling
- Duplicate transaction ID detection
- Idempotency key handling
- Input validation
- Retry handling
- Transaction status transitions
- Pending transaction processing
- Insufficient balance handling
- Multiple pending transaction processing
- JSON input and output generation

## 3. Test Execution

**Testing Framework:** JUnit 5

**Programming Language:** Java 18

**Build Tool:** Maven

**Test Types:**
- Unit Testing
- Functional Testing
- Regression Testing

**Execution Environment:** Local IntelliJ IDEA

**Functional Test Cases Implemented:** 12

**Functional Test Execution Result:** All 12 functional tests passed.

## 4. Expected Test Result

- Valid credit and debit transactions should be processed correctly.
- Invalid transactions should be rejected without affecting account balances.
- Duplicate transaction IDs should not result in duplicate processing.
- Duplicate idempotency keys should prevent repeated transaction processing.
- Out-of-order transactions should be processed according to sequence numbers.
- Pending transactions should be processed when the expected sequence becomes available.
- Insufficient balance transactions should fail without changing the account balance.
- Temporary failures should be retried up to the configured maximum number of attempts.
- Transactions should be marked with the appropriate processing status.
- The generated JSON output should contain accurate transaction statuses and final account balances.

## 5. Test Evidence

Execution evidence is maintained in the project's `evidence/` directory.

The evidence includes:

- Functional and unit test execution results from IntelliJ IDEA.
- Application execution console output.
- Generated transaction output JSON.
- Project structure showing source code, tests, documentation, and sample data.
