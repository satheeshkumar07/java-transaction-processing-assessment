
package com.assessment.model;

public class TransactionResult {

    private String transactionId;
    private String status;

    public TransactionResult() {
    }

    public TransactionResult(
            String transactionId,
            String status) {

        this.transactionId = transactionId;
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
    }
}
