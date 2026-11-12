
package com.assessment.model;

import java.math.BigDecimal;
import java.util.List;

public class TransactionSummary {

    private String accountId;
    private String currency;
    private int transactionsProcessed;
    private int transactionsFailed;
    private BigDecimal finalBalance;
    private List<TransactionResult> transactionResults;

    public TransactionSummary() {
    }

    public TransactionSummary(
            String accountId,
            String currency,
            int transactionsProcessed,
            int transactionsFailed,
            BigDecimal finalBalance,
            List<TransactionResult> transactionResults) {

        this.accountId = accountId;
        this.currency = currency;
        this.transactionsProcessed = transactionsProcessed;
        this.transactionsFailed = transactionsFailed;
        this.finalBalance = finalBalance;
        this.transactionResults = transactionResults;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCurrency() {
        return currency;
    }

    public int getTransactionsProcessed() {
        return transactionsProcessed;
    }

    public int getTransactionsFailed() {
        return transactionsFailed;
    }

    public BigDecimal getFinalBalance() {
        return finalBalance;
    }

    public List<TransactionResult> getTransactionResults() {
        return transactionResults;
    }
}
