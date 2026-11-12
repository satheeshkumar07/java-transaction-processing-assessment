
package com.assessment.exception;

public class NonRetryableProcessingException extends RuntimeException {

    public NonRetryableProcessingException(String message) {
        super(message);
    }
}
