package com.simeonatanasov.employees.csv;

import java.util.List;

public class CsvParsingException extends RuntimeException {

    private final List<String> errors;

    public CsvParsingException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public CsvParsingException(List<CsvValidationError> validationErrors) {
        super("CSV validation failed.");
        this.errors = validationErrors.stream()
                .map(CsvValidationError::toString)
                .toList();
    }

    public List<String> getErrors() {
        return errors;
    }
}
