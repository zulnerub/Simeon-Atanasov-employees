package com.simeonatanasov.employees.dto;

import com.simeonatanasov.employees.model.CollaborationResult;

import java.util.List;

public record AnalyzeResponse(
        Long employeeId1,
        Long employeeId2,
        long totalDaysWorked,
        String message,
        List<ProjectOverlapResponse> projects
) {
    public static AnalyzeResponse from(CollaborationResult result) {
        return new AnalyzeResponse(
                result.pair().employeeId1(),
                result.pair().employeeId2(),
                result.totalDaysWorked(),
                "Longest collaboration found.",
                result.projects().stream().map(ProjectOverlapResponse::from).toList()
        );
    }

    public static AnalyzeResponse empty() {
        return new AnalyzeResponse(
                null,
                null,
                0,
                "No employees have worked together on common projects.",
                List.of()
        );
    }
}
