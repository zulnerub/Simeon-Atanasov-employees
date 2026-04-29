package com.simeonatanasov.employees.csv;

public record CsvValidationError(long lineNumber, String message) {
}
