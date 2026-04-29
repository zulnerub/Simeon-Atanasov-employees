package com.simeonatanasov.employees.exception;

public record CsvValidationError(long lineNumber, String message) {
}
