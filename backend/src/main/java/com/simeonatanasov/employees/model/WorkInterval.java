package com.simeonatanasov.employees.model;

import java.time.LocalDate;

public record WorkInterval(LocalDate start, LocalDate end) {
    public WorkInterval {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Interval dates are required.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Interval end cannot be before interval start.");
        }
    }

    public boolean overlapsOrTouches(WorkInterval other) {
        return !other.start().isAfter(end.plusDays(1));
    }

    public WorkInterval merge(WorkInterval other) {
        LocalDate mergedStart = start.isBefore(other.start()) ? start : other.start();
        LocalDate mergedEnd = end.isAfter(other.end()) ? end : other.end();
        return new WorkInterval(mergedStart, mergedEnd);
    }
}
