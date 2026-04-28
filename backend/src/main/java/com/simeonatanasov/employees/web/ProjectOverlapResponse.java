package com.simeonatanasov.employees.web;

import com.simeonatanasov.employees.collaboration.ProjectOverlap;

public record ProjectOverlapResponse(
        long employeeId1,
        long employeeId2,
        long projectId,
        long daysWorked
) {
    public static ProjectOverlapResponse from(ProjectOverlap overlap) {
        return new ProjectOverlapResponse(
                overlap.employeeId1(),
                overlap.employeeId2(),
                overlap.projectId(),
                overlap.daysWorked()
        );
    }
}
