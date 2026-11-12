package com.assessment.model;


import java.math.BigDecimal;

public class Transaction {

    private String transactionId;
    private String accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String currency;
    private int sequenceNumber;
    private String idempotencyKey;
    private TransactionStatus status;

    public Transaction() {
    }

    public Transaction(String transactionId, String accountId,
                       TransactionType transactionType, BigDecimal amount,
                       String currency, int sequenceNumber,
                       String idempotencyKey) {

        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.sequenceNumber = sequenceNumber;
        this.idempotencyKey = idempotencyKey;
        this.status = TransactionStatus.RECEIVED;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }


    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }


    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

}

