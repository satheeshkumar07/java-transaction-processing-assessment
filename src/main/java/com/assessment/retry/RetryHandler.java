
package com.assessment.retry;

import com.assessment.exception.RetryableProcessingException;
import com.assessment.exception.NonRetryableProcessingException;
import com.assessment.model.Transaction;
import com.assessment.model.TransactionStatus;

public class RetryHandler {

    @FunctionalInterface
    public interface RetryableOperation {
        void execute() throws RetryableProcessingException;
    }


    public boolean execute(
            Transaction transaction,
            RetryableOperation operation,
            int maxAttempts) {

        if (maxAttempts <= 0) {
            throw new IllegalArgumentException(
                    "Maximum attempts must be greater than zero");
        }

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            transaction.setStatus(TransactionStatus.PROCESSING);

            try {
                operation.execute();

                transaction.setStatus(TransactionStatus.PROCESSED);

                System.out.println(
                        "Transaction succeeded on attempt " + attempt);

                return true;

            } catch (NonRetryableProcessingException exception) {

                transaction.setStatus(TransactionStatus.FAILED);

                System.out.println(
                        "Non-retryable failure: " + exception.getMessage());

                return false;

            } catch (RetryableProcessingException exception) {

                System.out.println(
                        "Attempt " + attempt + " failed: "
                                + exception.getMessage());

                if (attempt < maxAttempts) {
                    transaction.setStatus(TransactionStatus.RETRY_PENDING);
                } else {
                    transaction.setStatus(TransactionStatus.FAILED);

                    System.out.println(
                            "Maximum retry attempts exhausted");
                }
            }
            catch (RuntimeException exception) {

                transaction.setStatus(TransactionStatus.FAILED);

                System.out.println(
                        "Unexpected processing error: " + exception.getMessage());

                return false;
            }
        }

        return false;
    }

}
