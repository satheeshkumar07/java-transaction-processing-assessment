
package com.assessment.unit;

import com.assessment.model.Transaction;
import com.assessment.model.TransactionStatus;
import com.assessment.model.TransactionType;
import com.assessment.repository.TransactionRepository;
import com.assessment.service.TransactionProcessingService;
import com.assessment.validator.TransactionValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionProcessingServiceTest {

    private TransactionRepository repository;
    private TransactionProcessingService service;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();

        TransactionValidator validator =
                new TransactionValidator();

        service = new TransactionProcessingService(
                repository, validator);
    }

    @Test
    void shouldProcessCreditTransaction() {

        Transaction transaction = createTransaction(
                "TXN-101", TransactionType.CREDIT,
                "1000.00", "KEY-101");

        service.processTransaction(transaction);

        assertEquals(TransactionStatus.PROCESSED,
                transaction.getStatus());

        assertEquals(new BigDecimal("1000.00"),
                repository.getAccountBalance("ACC-101"));
    }

    @Test
    void shouldProcessDebitTransaction() {

        Transaction credit = createTransaction(
                "TXN-102", TransactionType.CREDIT,
                "1000.00", "KEY-102");

        service.processTransaction(credit);
        Transaction debit = new Transaction(
                "TXN-103",
                "ACC-101",
                TransactionType.DEBIT,
                new BigDecimal("300.00"),
                "INR",
                2,
                "KEY-103"
        );
        service.processTransaction(debit);

        assertEquals(TransactionStatus.PROCESSED,
                debit.getStatus());

        assertEquals(new BigDecimal("700.00"),
                repository.getAccountBalance("ACC-101"));
    }

    @Test
    void shouldRejectDebitWhenBalanceIsInsufficient() {

        Transaction debit = createTransaction(
                "TXN-104", TransactionType.DEBIT,
                "500.00", "KEY-104");

        service.processTransaction(debit);

        assertEquals(TransactionStatus.FAILED,
                debit.getStatus());

        assertEquals(BigDecimal.ZERO,
                repository.getAccountBalance("ACC-101"));
    }

    @Test
    void shouldNotProcessDuplicateRequestTwice() {

        Transaction transaction = createTransaction(
                "TXN-105", TransactionType.CREDIT,
                "1000.00", "KEY-105");

        service.processTransaction(transaction);

        // Submit the same request again
        service.processTransaction(transaction);

        assertEquals(TransactionStatus.DUPLICATE,
                transaction.getStatus());

        assertEquals(new BigDecimal("1000.00"),
                repository.getAccountBalance("ACC-101"));
    }

    private Transaction createTransaction(
            String transactionId,
            TransactionType type,
            String amount,
            String idempotencyKey) {

        return new Transaction(
                transactionId,
                "ACC-101",
                type,
                new BigDecimal(amount),
                "INR",
                1,
                idempotencyKey
        );
    }

    @Test
    void shouldProcessTransactionsInCorrectSequence() {

        // Sequence 2 arrives first
        Transaction second = new Transaction(
                "TXN-202",
                "ACC-101",
                TransactionType.CREDIT,
                new BigDecimal("200.00"),
                "INR",
                2,
                "KEY-202"
        );

        service.processTransaction(second);

        // It should wait because sequence 1 is missing
        assertEquals(TransactionStatus.RECEIVED, second.getStatus());

        assertEquals(BigDecimal.ZERO,
                repository.getAccountBalance("ACC-101"));

        // Sequence 1 arrives
        Transaction first = new Transaction(
                "TXN-201",
                "ACC-101",
                TransactionType.CREDIT,
                new BigDecimal("100.00"),
                "INR",
                1,
                "KEY-201"
        );

        service.processTransaction(first);

        // Both transactions should now be processed
        assertEquals(TransactionStatus.PROCESSED, first.getStatus());

        assertEquals(TransactionStatus.PROCESSED, second.getStatus());

        assertEquals(new BigDecimal("300.00"),
                repository.getAccountBalance("ACC-101"));
    }




    @Test
    void shouldRetryTransactionAndSucceed() {

        Transaction transaction = new Transaction(
                "TXN-2005",
                "ACC-500",
                TransactionType.CREDIT,
                new BigDecimal("500.00"),
                "INR",
                1,
                "IDEMP-2005"
        );

         service.processTransaction(
                transaction, 1);

        assertEquals(
                TransactionStatus.PROCESSED,
                transaction.getStatus());

        assertEquals(
                new BigDecimal("500.00"),
                repository.getAccountBalance("ACC-500"));
    }


}
