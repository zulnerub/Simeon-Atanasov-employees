package com.simeonatanasov.employees.collaboration;

import java.util.List;

public record CollaborationResult(
        EmployeePair pair,
        long totalDaysWorked,
        List<ProjectOverlap> projects
) {
    public CollaborationResult {
        projects = List.copyOf(projects);
    }
}
