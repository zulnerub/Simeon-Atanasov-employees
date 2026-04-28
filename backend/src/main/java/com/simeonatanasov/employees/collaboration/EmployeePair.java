package com.simeonatanasov.employees.collaboration;

public record EmployeePair(long employeeId1, long employeeId2) {
    public EmployeePair {
        if (employeeId1 <= 0 || employeeId2 <= 0) {
            throw new IllegalArgumentException("Employee IDs must be positive.");
        }
        if (employeeId1 == employeeId2) {
            throw new IllegalArgumentException("Employee pair must contain two different employees.");
        }
        if (employeeId1 > employeeId2) {
            long temp = employeeId1;
            employeeId1 = employeeId2;
            employeeId2 = temp;
        }
    }
}
