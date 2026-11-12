
package com.assessment;

import com.assessment.input.TransactionJsonReader;
import com.assessment.model.Transaction;
import com.assessment.repository.TransactionRepository;
import com.assessment.service.TransactionProcessingService;
import com.assessment.validator.TransactionValidator;

import com.assessment.input.TransactionJsonWriter;
import com.assessment.model.TransactionSummary;
import com.assessment.model.TransactionResult;
import com.assessment.model.TransactionStatus;

import java.util.ArrayList;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class TransactionApplication {

    public static void main(String[] args) {

        try {
            // Initialize application components
            TransactionRepository repository =
                    new TransactionRepository();

            TransactionValidator validator =
                    new TransactionValidator();

            TransactionProcessingService service =
                    new TransactionProcessingService(
                            repository, validator);

            TransactionJsonReader reader =
                    new TransactionJsonReader();

            // Read transactions from JSON
            List<Transaction> transactions =
                    reader.readTransactions(
                            "sample-data/input.json");

            System.out.println("===== TRANSACTION PROCESSING STARTED =====");

            // Process each transaction
            for (Transaction transaction : transactions) {
                service.processTransaction(transaction);
            }

            int processedCount = 0;
            int failedCount = 0;

            List<TransactionResult> results = new ArrayList<>();

            for (Transaction transaction : transactions) {

                if (transaction.getStatus() == TransactionStatus.PROCESSED) {
                    processedCount++;
                }

                if (transaction.getStatus() == TransactionStatus.FAILED) {
                    failedCount++;
                }

                results.add(new TransactionResult(
                        transaction.getTransactionId(),
                        transaction.getStatus().name()
                ));
            }

            String accountId = transactions.get(0).getAccountId();
            String currency = transactions.get(0).getCurrency();

            BigDecimal finalBalance =
                    repository.getAccountBalance(accountId);

            TransactionSummary summary = new TransactionSummary(
                    accountId,
                    currency,
                    processedCount,
                    failedCount,
                    finalBalance,
                    results
            );

// Generate output JSON
            TransactionJsonWriter writer = new TransactionJsonWriter();

            writer.writeSummary(
                    summary,
                    "sample-data/output.json"
            );

            System.out.println("Output JSON generated successfully.");


            System.out.println("\n===== TRANSACTION RESULTS =====");

            for (Transaction transaction : transactions) {

                System.out.println(
                        "Transaction ID: "
                                + transaction.getTransactionId());

                System.out.println(
                        "Status: " + transaction.getStatus());

                System.out.println("--------------------------------");
            }

            // Display final account balance
            if (!transactions.isEmpty()) {

                 accountId =
                        transactions.get(0).getAccountId();

                BigDecimal balance =
                        repository.getAccountBalance(accountId);

                System.out.println(
                        "Account ID: " + accountId);

                System.out.println(
                        "Final Balance: " + balance);
            }

            System.out.println(
                    "\n===== PROCESSING COMPLETED =====");

        } catch (IOException exception) {

            System.out.println(
                    "Error reading transaction input: "
                            + exception.getMessage());

        } catch (Exception exception) {

            System.out.println(
                    "Application error: "
                            + exception.getMessage());

            exception.printStackTrace();
        }
    }
}
