package com.simeonatanasov.employees.collaboration;

import java.time.LocalDate;

public record WorkRecord(
        long employeeId,
        long projectId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
    public WorkRecord {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive.");
        }
        if (projectId <= 0) {
            throw new IllegalArgumentException("Project ID must be positive.");
        }
        if (dateFrom == null) {
            throw new IllegalArgumentException("DateFrom is required.");
        }
        if (dateTo == null) {
            throw new IllegalArgumentException("DateTo is required.");
        }
        if (dateTo.isBefore(dateFrom)) {
            throw new IllegalArgumentException("DateTo cannot be before DateFrom.");
        }
    }
}
