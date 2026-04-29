package com.simeonatanasov.employees.dto;

import java.util.List;

public record ApiErrorResponse(String message, List<String> errors) {
    public ApiErrorResponse(String message) {
        this(message, List.of(message));
    }
}
