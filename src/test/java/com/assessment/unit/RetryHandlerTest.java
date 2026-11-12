
package com.assessment.unit;

import com.assessment.exception.RetryableProcessingException;
import com.assessment.model.Transaction;
import com.assessment.model.TransactionStatus;
import com.assessment.model.TransactionType;
import com.assessment.retry.RetryHandler;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryHandlerTest {

    private final RetryHandler retryHandler = new RetryHandler();

    private Transaction createTransaction() {
        return new Transaction(
                "TXN-2005",
                "ACC-101",
                TransactionType.CREDIT,
                new BigDecimal("500.00"),
                "INR",
                1,
                "KEY-2005"
        );
    }

    @Test
    void shouldSucceedWhenFirstAttemptFails() {

        Transaction transaction = createTransaction();
        AtomicInteger attempts = new AtomicInteger(0);

        boolean result = retryHandler.execute(
                transaction,
                () -> {
                    if (attempts.incrementAndGet() == 1) {
                        throw new RetryableProcessingException(
                                "Temporary processing failure");
                    }
                },
                3
        );

        assertTrue(result);
        assertEquals(2, attempts.get());
        assertEquals(TransactionStatus.PROCESSED,
                transaction.getStatus());
    }

    @Test
    void shouldFailWhenAllRetryAttemptsAreExhausted() {

        Transaction transaction = createTransaction();
        AtomicInteger attempts = new AtomicInteger(0);

        boolean result = retryHandler.execute(
                transaction,
                () -> {
                    attempts.incrementAndGet();

                    throw new RetryableProcessingException(
                            "Temporary processing failure");
                },
                3
        );

        assertFalse(result);
        assertEquals(3, attempts.get());
        assertEquals(TransactionStatus.FAILED,
                transaction.getStatus());
    }
}
