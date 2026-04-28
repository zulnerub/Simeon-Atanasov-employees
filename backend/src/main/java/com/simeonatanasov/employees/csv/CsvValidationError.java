package com.simeonatanasov.employees.csv;

public record CsvValidationError(long lineNumber, String message) {
    @Override
    public String toString() {
        return "Line " + lineNumber + ": " + message;
    }
}
