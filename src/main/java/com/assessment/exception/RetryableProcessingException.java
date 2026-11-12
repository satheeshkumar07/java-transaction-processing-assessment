
package com.assessment.exception;

public class RetryableProcessingException extends RuntimeException {

    public RetryableProcessingException(String message) {
        super(message);
    }
}
