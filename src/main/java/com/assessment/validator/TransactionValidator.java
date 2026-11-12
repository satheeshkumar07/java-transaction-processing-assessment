
package com.assessment.validator;

import com.assessment.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionValidator {

    public List<String> validate(Transaction transaction) {

        List<String> errors = new ArrayList<>();

        if (transaction == null) {
            errors.add("Transaction cannot be null");
            return errors;
        }

        if (transaction.getTransactionId() == null ||
                transaction.getTransactionId().isBlank()) {
            errors.add("Transaction ID is required");
        }

        if (transaction.getAccountId() == null ||
                transaction.getAccountId().isBlank()) {
            errors.add("Account ID is required");
        }

        if (transaction.getTransactionType() == null) {
            errors.add("Transaction type is required");
        }

        if (transaction.getAmount() == null ||
                transaction.getAmount().signum() <= 0) {
            errors.add("Amount must be greater than zero");
        }

        if (transaction.getCurrency() == null ||
                !transaction.getCurrency().matches("[A-Za-z]{3}")) {
            errors.add("Currency must be a three-letter code");
        }

        if (transaction.getSequenceNumber() <= 0) {
            errors.add("Sequence number must be greater than zero");
        }

        if (transaction.getIdempotencyKey() == null ||
                transaction.getIdempotencyKey().isBlank()) {
            errors.add("Idempotency key is required");
        }

        return errors;
    }
}

