
package com.assessment.repository;

import com.assessment.model.Transaction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

public class TransactionRepository {

    private final Map<String, Transaction> transactions =
            new HashMap<>();

    private final Map<String, BigDecimal> accountBalances =
            new HashMap<>();

    private final Set<String> processedIdempotencyKeys =
            new HashSet<>();

    private final Set<String> reservedIdempotencyKeys =
            new HashSet<>();

    private final Map<String, Integer> nextSequenceNumbers =
            new HashMap<>();

    private final Map<String, Map<Integer, Transaction>>
            pendingTransactions = new HashMap<>();

    public void saveTransaction(Transaction transaction) {
        transactions.put(transaction.getTransactionId(), transaction);
    }

    public Optional<Transaction> findTransaction(String transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }

    public BigDecimal getAccountBalance(String accountId) {
        return accountBalances.getOrDefault(
                accountId, BigDecimal.ZERO);
    }

    public void updateAccountBalance(
            String accountId, BigDecimal balance) {
        accountBalances.put(accountId, balance);
    }

    public boolean isIdempotencyKeyProcessed(String key) {
        return processedIdempotencyKeys.contains(key);
    }

    public boolean reserveIdempotencyKey(String key) {
        if (processedIdempotencyKeys.contains(key)) {
            return false;
        }
        return reservedIdempotencyKeys.add(key);
    }

    public void markIdempotencyKeyProcessed(String key) {
        reservedIdempotencyKeys.remove(key);
        processedIdempotencyKeys.add(key);
    }

    public int getNextExpectedSequence(String accountId) {
        return nextSequenceNumbers.getOrDefault(accountId, 1);
    }

    public void updateNextExpectedSequence(
            String accountId, int sequenceNumber) {
        nextSequenceNumbers.put(accountId, sequenceNumber);
    }

    public void savePendingTransaction(Transaction transaction) {
        Map<Integer, Transaction> accountTransactions =
                pendingTransactions.computeIfAbsent(
                        transaction.getAccountId(),
                        key -> new TreeMap<>());

        if (accountTransactions.containsKey(
                transaction.getSequenceNumber())) {
            throw new IllegalStateException(
                    "A transaction already exists for this account sequence");
        }

        accountTransactions.put(
                transaction.getSequenceNumber(), transaction);
    }

    public Transaction getPendingTransaction(
            String accountId, int sequenceNumber) {

        Map<Integer, Transaction> accountTransactions =
                pendingTransactions.get(accountId);

        if (accountTransactions == null) {
            return null;
        }

        return accountTransactions.get(sequenceNumber);
    }

    public void removePendingTransaction(
            String accountId, int sequenceNumber) {

        Map<Integer, Transaction> accountTransactions =
                pendingTransactions.get(accountId);

        if (accountTransactions != null) {
            accountTransactions.remove(sequenceNumber);

            if (accountTransactions.isEmpty()) {
                pendingTransactions.remove(accountId);
            }
        }
    }

}
