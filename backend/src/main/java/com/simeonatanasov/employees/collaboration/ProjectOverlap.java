package com.simeonatanasov.employees.collaboration;

public record ProjectOverlap(
        long employeeId1,
        long employeeId2,
        long projectId,
        long daysWorked
) {
    public ProjectOverlap {
        if (employeeId1 <= 0 || employeeId2 <= 0 || projectId <= 0) {
            throw new IllegalArgumentException("IDs must be positive.");
        }
        if (employeeId1 == employeeId2) {
            throw new IllegalArgumentException("Project overlap must contain two different employees.");
        }
        if (daysWorked <= 0) {
            throw new IllegalArgumentException("Days worked must be positive.");
        }
    }
}
