
package com.assessment.functional;

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

class TransactionFunctionalTest {

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

    private Transaction createTransaction(
            String transactionId,
            String accountId,
            TransactionType type,
            String amount,
            int sequence,
            String idempotencyKey) {

        return new Transaction(
                transactionId,
                accountId,
                type,
                new BigDecimal(amount),
                "INR",
                sequence,
                idempotencyKey
        );
    }

    @Test
    void shouldCompleteCreditAndDebitWorkflow() {

        Transaction credit = createTransaction(
                "FT-101", "ACC-101",
                TransactionType.CREDIT,
                "1000.00", 1, "KEY-101");

        Transaction debit = createTransaction(
                "FT-102", "ACC-101",
                TransactionType.DEBIT,
                "300.00", 2, "KEY-102");

        service.processTransaction(credit);
        service.processTransaction(debit);

        assertEquals(TransactionStatus.PROCESSED,
                credit.getStatus());

        assertEquals(TransactionStatus.PROCESSED,
                debit.getStatus());

        assertEquals(new BigDecimal("700.00"),
                repository.getAccountBalance("ACC-101"));
    }

    @Test
    void shouldProcessOutOfOrderTransactions() {

        Transaction second = createTransaction(
                "FT-202", "ACC-202",
                TransactionType.CREDIT,
                "200.00", 2, "KEY-202");

        Transaction first = createTransaction(
                "FT-201", "ACC-202",
                TransactionType.CREDIT,
                "100.00", 1, "KEY-201");

        service.processTransaction(second);

        assertEquals(TransactionStatus.RECEIVED,
                second.getStatus());

        service.processTransaction(first);

        assertEquals(TransactionStatus.PROCESSED,
                first.getStatus());

        assertEquals(TransactionStatus.PROCESSED,
                second.getStatus());

        assertEquals(new BigDecimal("300.00"),
                repository.getAccountBalance("ACC-202"));
    }

    @Test
    void shouldRejectDuplicateTransaction() {

        Transaction original = createTransaction(
                "FT-301", "ACC-301",
                TransactionType.CREDIT,
                "500.00", 1, "KEY-301");

        Transaction duplicate = createTransaction(
                "FT-301", "ACC-301",
                TransactionType.CREDIT,
                "500.00", 1, "KEY-302");

        service.processTransaction(original);
        service.processTransaction(duplicate);

        assertEquals(TransactionStatus.PROCESSED,
                original.getStatus());

        assertEquals(TransactionStatus.DUPLICATE,
                duplicate.getStatus());

        assertEquals(new BigDecimal("500.00"),
                repository.getAccountBalance("ACC-301"));
    }

    @Test
    void shouldRejectInvalidTransaction() {

        Transaction invalid = createTransaction(
                "FT-401", "ACC-401",
                TransactionType.CREDIT,
                "-100.00", 1, "KEY-401");

        service.processTransaction(invalid);

        assertEquals(TransactionStatus.FAILED,
                invalid.getStatus());

        assertEquals(BigDecimal.ZERO,
                repository.getAccountBalance("ACC-401"));
    }

    @Test
    void shouldHandleRetryExhaustion() {

        Transaction transaction = createTransaction(
                "FT-501", "ACC-501",
                TransactionType.CREDIT,
                "800.00", 1, "KEY-501");

        service.processTransaction(transaction, 3);

        assertEquals(TransactionStatus.FAILED,
                transaction.getStatus());

        assertEquals(BigDecimal.ZERO,
                repository.getAccountBalance("ACC-501"));
    }

    @Test
    void shouldRejectDifferentTransactionWithSameIdempotencyKey() {

        Transaction original = createTransaction(
                "FT-601", "ACC-601",
                TransactionType.CREDIT,
                "500.00", 1, "SAME-KEY");

        Transaction duplicate = createTransaction(
                "FT-602", "ACC-601",
                TransactionType.CREDIT,
                "300.00", 2, "SAME-KEY");

        service.processTransaction(original);
        service.processTransaction(duplicate);

        assertEquals(
                TransactionStatus.PROCESSED,
                original.getStatus());

        assertEquals(
                TransactionStatus.DUPLICATE,
                duplicate.getStatus());

        assertEquals(
                new BigDecimal("500.00"),
                repository.getAccountBalance("ACC-601"));
    }

    @Test
    void shouldNotProcessDuplicatePendingTransaction() {

        Transaction second = createTransaction(
                "FT-702", "ACC-702",
                TransactionType.CREDIT,
                "200.00", 2, "KEY-702");

        Transaction duplicate = createTransaction(
                "FT-702", "ACC-702",
                TransactionType.CREDIT,
                "200.00", 2, "KEY-702");

        Transaction first = createTransaction(
                "FT-701", "ACC-702",
                TransactionType.CREDIT,
                "100.00", 1, "KEY-701");

        service.processTransaction(second);

        assertEquals(
                TransactionStatus.RECEIVED,
                second.getStatus());

        service.processTransaction(duplicate);

        assertEquals(
                TransactionStatus.DUPLICATE,
                duplicate.getStatus());

        service.processTransaction(first);

        assertEquals(
                TransactionStatus.PROCESSED,
                first.getStatus());

        assertEquals(
                TransactionStatus.PROCESSED,
                second.getStatus());

        assertEquals(
                new BigDecimal("300.00"),
                repository.getAccountBalance("ACC-702"));
    }


    @Test
    void shouldRejectSameIdempotencyKeyWhileOriginalIsPending() {

        Transaction original = createTransaction(
                "FT-801", "ACC-801",
                TransactionType.CREDIT,
                "200.00", 2, "SAME-KEY-801");

        Transaction duplicate = createTransaction(
                "FT-802", "ACC-801",
                TransactionType.CREDIT,
                "300.00", 3, "SAME-KEY-801");

        service.processTransaction(original);

        assertEquals(
                TransactionStatus.RECEIVED,
                original.getStatus());

        service.processTransaction(duplicate);

        assertEquals(
                TransactionStatus.DUPLICATE,
                duplicate.getStatus());
    }

    @Test
    void shouldRejectZeroAmount() {

        Transaction transaction = createTransaction(
                "FT-901", "ACC-901",
                TransactionType.CREDIT,
                "0.00", 1, "KEY-901");

        service.processTransaction(transaction);

        assertEquals(TransactionStatus.FAILED,
                transaction.getStatus());

        assertEquals(BigDecimal.ZERO,
                repository.getAccountBalance("ACC-901"));
    }

    @Test
    void shouldRejectInvalidCurrency() {

        Transaction transaction = createTransaction(
                "FT-902", "ACC-902",
                TransactionType.CREDIT,
                "100.00", 1, "KEY-902");

        transaction.setCurrency("IN");

        service.processTransaction(transaction);

        assertEquals(TransactionStatus.FAILED,
                transaction.getStatus());

        assertEquals(BigDecimal.ZERO,
                repository.getAccountBalance("ACC-902"));
    }

    @Test
    void shouldNotChangeBalanceWhenDebitExceedsBalance() {

        Transaction debit = createTransaction(
                "FT-903", "ACC-903",
                TransactionType.DEBIT,
                "500.00", 1, "KEY-903");

        service.processTransaction(debit);

        assertEquals(TransactionStatus.FAILED,
                debit.getStatus());

        assertEquals(BigDecimal.ZERO,
                repository.getAccountBalance("ACC-903"));
    }

    @Test
    void shouldProcessMultiplePendingTransactionsInSequence() {

        Transaction third = createTransaction(
                "FT-1003", "ACC-1001",
                TransactionType.CREDIT,
                "300.00", 3, "KEY-1003");

        Transaction second = createTransaction(
                "FT-1002", "ACC-1001",
                TransactionType.CREDIT,
                "200.00", 2, "KEY-1002");

        Transaction first = createTransaction(
                "FT-1001", "ACC-1001",
                TransactionType.CREDIT,
                "100.00", 1, "KEY-1001");

        service.processTransaction(third);
        service.processTransaction(second);

        assertEquals(TransactionStatus.RECEIVED,
                third.getStatus());

        assertEquals(TransactionStatus.RECEIVED,
                second.getStatus());

        service.processTransaction(first);

        assertEquals(TransactionStatus.PROCESSED,
                first.getStatus());

        assertEquals(TransactionStatus.PROCESSED,
                second.getStatus());

        assertEquals(TransactionStatus.PROCESSED,
                third.getStatus());

        assertEquals(new BigDecimal("600.00"),
                repository.getAccountBalance("ACC-1001"));
    }

}
