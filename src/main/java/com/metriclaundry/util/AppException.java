package com.metriclaundry.util;

/** A business-rule violation with a message that is safe to show to the user. */
public class AppException extends RuntimeException {
    public AppException(String message) { super(message); }
    public AppException(String message, Throwable cause) { super(message, cause); }
}
