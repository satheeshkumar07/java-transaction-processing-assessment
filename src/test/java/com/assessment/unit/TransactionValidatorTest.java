
package com.assessment.unit;

import com.assessment.model.Transaction;
import com.assessment.model.TransactionType;
import com.assessment.validator.TransactionValidator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionValidatorTest {

    private final TransactionValidator validator =
            new TransactionValidator();

    @Test
    void shouldAcceptValidTransaction() {

        Transaction transaction = new Transaction(
                "TXN-201",
                "ACC-101",
                TransactionType.CREDIT,
                new BigDecimal("1000.00"),
                "INR",
                1,
                "KEY-201"
        );

        List<String> errors = validator.validate(transaction);

        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldRejectNullTransaction() {

        List<String> errors = validator.validate(null);

        assertEquals(1, errors.size());
        assertEquals("Transaction cannot be null", errors.get(0));
    }

    @Test
    void shouldRejectNegativeAmount() {

        Transaction transaction = new Transaction(
                "TXN-202",
                "ACC-101",
                TransactionType.CREDIT,
                new BigDecimal("-500"),
                "INR",
                1,
                "KEY-202"
        );

        List<String> errors = validator.validate(transaction);

        assertTrue(errors.contains("Amount must be greater than zero"));
    }

    @Test
    void shouldRejectMissingAccountId() {

        Transaction transaction = new Transaction(
                "TXN-203",
                "",
                TransactionType.CREDIT,
                new BigDecimal("500"),
                "INR",
                1,
                "KEY-203"
        );

        List<String> errors = validator.validate(transaction);

        assertTrue(errors.contains("Account ID is required"));
    }

    @Test
    void shouldRejectInvalidSequenceNumber() {

        Transaction transaction = new Transaction(
                "TXN-204",
                "ACC-101",
                TransactionType.CREDIT,
                new BigDecimal("500"),
                "INR",
                0,
                "KEY-204"
        );

        List<String> errors = validator.validate(transaction);

        assertTrue(errors.contains("Sequence number must be greater than zero"));
    }
}
