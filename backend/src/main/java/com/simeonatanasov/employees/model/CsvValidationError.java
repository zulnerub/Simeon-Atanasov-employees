package com.simeonatanasov.employees.model;

public record CsvValidationError(long lineNumber, String message) {
}
