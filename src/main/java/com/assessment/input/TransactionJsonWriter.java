
package com.assessment.input;

import com.assessment.model.TransactionSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

public class TransactionJsonWriter {

    private final ObjectMapper objectMapper;

    public TransactionJsonWriter() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(
                SerializationFeature.INDENT_OUTPUT);
    }

    public void writeSummary(
            TransactionSummary summary,
            String filePath) throws IOException {

        File outputFile = new File(filePath);

        File parentDirectory = outputFile.getParentFile();

        if (parentDirectory != null) {
            parentDirectory.mkdirs();
        }

        objectMapper.writeValue(outputFile, summary);
    }
}
