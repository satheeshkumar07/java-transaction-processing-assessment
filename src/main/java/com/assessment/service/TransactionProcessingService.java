
package com.assessment.service;

import com.assessment.model.Transaction;
import com.assessment.model.TransactionStatus;
import com.assessment.model.TransactionType;
import com.assessment.repository.TransactionRepository;
import com.assessment.validator.TransactionValidator;
import com.assessment.retry.RetryHandler;
import com.assessment.exception.RetryableProcessingException;
import com.assessment.exception.NonRetryableProcessingException;
import java.util.concurrent.atomic.AtomicInteger;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;


import java.math.BigDecimal;
import java.util.List;

public class TransactionProcessingService {

    private final TransactionRepository repository;
    private final TransactionValidator validator;
    private final RetryHandler retryHandler = new RetryHandler();
    private static final int MAX_ATTEMPTS = 3;

    public TransactionProcessingService(
            TransactionRepository repository,
            TransactionValidator validator) {

        this.repository = repository;
        this.validator = validator;
    }


    public void processTransaction(
            Transaction transaction,
            int simulatedFailures) {

        if (simulatedFailures < 0) {
            throw new IllegalArgumentException(
                    "Simulated failures cannot be negative");
        }

        List<String> errors = validator.validate(transaction);

        if (!errors.isEmpty()) {
            transaction.setStatus(TransactionStatus.FAILED);
            repository.saveTransaction(transaction);
            return;
        }

        if (repository.isIdempotencyKeyProcessed(
                transaction.getIdempotencyKey())
                || !repository.reserveIdempotencyKey(
                transaction.getIdempotencyKey())) {

            transaction.setStatus(TransactionStatus.DUPLICATE);
            return;
        }

        Optional<Transaction> existing =
                repository.findTransaction(
                        transaction.getTransactionId());

        if (existing.isPresent()) {
            transaction.setStatus(TransactionStatus.DUPLICATE);
            return;
        }

        String accountId = transaction.getAccountId();

        int expectedSequence =
                repository.getNextExpectedSequence(accountId);

        if (transaction.getSequenceNumber() > expectedSequence) {

            repository.savePendingTransaction(transaction);
            repository.saveTransaction(transaction);
            return;
        }

        if (transaction.getSequenceNumber() < expectedSequence) {
            transaction.setStatus(TransactionStatus.DUPLICATE);
            return;
        }

        processInSequence(transaction, simulatedFailures);

        processPendingTransactions(accountId);
    }




    public void processTransaction(Transaction transaction) {
        processTransaction(transaction, 0);
    }

    private void processInSequence(
            Transaction transaction,
            int simulatedFailures) {

        String accountId = transaction.getAccountId();
        BigDecimal amount = transaction.getAmount();

        AtomicInteger failuresRemaining =
                new AtomicInteger(simulatedFailures);

        boolean success = retryHandler.execute(
                transaction,
                () -> {

                    // Simulate temporary technical failures
                    if (failuresRemaining.getAndDecrement() > 0) {
                        throw new RetryableProcessingException(
                                "Simulated temporary processing failure");
                    }

                    BigDecimal currentBalance =
                            repository.getAccountBalance(accountId);

                    BigDecimal updatedBalance;

                    if (transaction.getTransactionType()
                            == TransactionType.CREDIT) {

                        updatedBalance = currentBalance.add(amount);

                    } else {

                        if (currentBalance.compareTo(amount) < 0) {
                            throw new NonRetryableProcessingException(
                                    "Insufficient account balance");
                        }

                        updatedBalance = currentBalance.subtract(amount);
                    }

                    repository.updateAccountBalance(
                            accountId, updatedBalance);
                },
                MAX_ATTEMPTS
        );

        if (success) {
            repository.markIdempotencyKeyProcessed(
                    transaction.getIdempotencyKey());
        }

        repository.saveTransaction(transaction);

        repository.updateNextExpectedSequence(
                accountId,
                transaction.getSequenceNumber() + 1);
    }

    private void processPendingTransactions(String accountId) {

        while (true) {

            int nextSequence =
                    repository.getNextExpectedSequence(accountId);


            Transaction pendingTransaction =
                    repository.getPendingTransaction(
                            accountId, nextSequence);

            if (pendingTransaction == null) {
                break;
            }

            repository.removePendingTransaction(
                    accountId, nextSequence);

            processInSequence(pendingTransaction, 0);
        }
    }

}
