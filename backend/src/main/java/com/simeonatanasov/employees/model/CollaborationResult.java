package com.simeonatanasov.employees.model;

import java.util.List;

public record CollaborationResult(
        long totalDaysWorked,
        EmployeePair pair,
        List<ProjectOverlap> projects
) {
    public CollaborationResult {
        projects = List.copyOf(projects);
    }
}
