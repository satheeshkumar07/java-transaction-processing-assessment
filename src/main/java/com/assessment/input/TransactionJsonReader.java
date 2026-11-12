
package com.assessment.input;

import com.assessment.model.Transaction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TransactionJsonReader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Transaction> readTransactions(String filePath)
            throws IOException {

        return objectMapper.readValue(
                new File(filePath),
                new TypeReference<List<Transaction>>() {}
        );
    }
}
