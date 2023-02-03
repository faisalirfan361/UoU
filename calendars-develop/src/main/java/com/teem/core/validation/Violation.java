package com.UoU.core.validation;

/**
 * Simplified view of javax.validation ConstraintViolation we can use in places where we don't want
 * to pass around or expose a full ConstraintViolation or exception.
 */
public record Violation(String field, String message) {
}
