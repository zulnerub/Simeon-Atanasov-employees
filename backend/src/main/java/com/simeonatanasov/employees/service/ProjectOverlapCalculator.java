package com.simeonatanasov.employees.service;

import com.simeonatanasov.employees.model.ProjectOverlap;
import com.simeonatanasov.employees.model.WorkRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProjectOverlapCalculator {
    private final WorkRecordOptimiser workRecordOptimiser;

    public ProjectOverlapCalculator(WorkRecordOptimiser workRecordOptimiser) {
        this.workRecordOptimiser = workRecordOptimiser;
    }


    public List<ProjectOverlap> findOverlaps(List<WorkRecord> records) {
        Map<Long, List<WorkRecord>> recordsByProject = workRecordOptimiser.optimizeRecordsPerProject(records);

        List<ProjectOverlap> overlaps = new ArrayList<>();
        for (Map.Entry<Long, List<WorkRecord>> entry : recordsByProject.entrySet()) {
            overlaps.addAll(findProjectOverlaps(entry.getKey(), entry.getValue()));
        }
        return overlaps;
    }

    private List<ProjectOverlap> findProjectOverlaps(long projectId, List<WorkRecord> projectRecords) {
        Map<Long, List<WorkRecord>> workRecordsByEmployee = new HashMap<>();
        for (WorkRecord record : projectRecords) {
            workRecordsByEmployee.computeIfAbsent(record.employeeId(), k -> new ArrayList<>()).add(record);
        }

        List<Long> sortedEmployeeIds = workRecordsByEmployee.keySet().stream().sorted().toList();
        List<ProjectOverlap> overlaps = new ArrayList<>();

        for (int i = 0; i < sortedEmployeeIds.size(); i++) {
            for (int j = i + 1; j < sortedEmployeeIds.size(); j++) {
                long days = calculateOverlapDays(
                        workRecordsByEmployee.get(sortedEmployeeIds.get(i)),
                        workRecordsByEmployee.get(sortedEmployeeIds.get(j)));
                if (days > 0) {
                    overlaps.add(new ProjectOverlap(sortedEmployeeIds.get(i), sortedEmployeeIds.get(j), projectId, days));
                }
            }
        }
        return overlaps;
    }

    private long calculateOverlapDays(List<WorkRecord> first, List<WorkRecord> second) {
        long totalDays = 0;
        int i = 0;
        int j = 0;

        while (i < first.size() && j < second.size()) {
            LocalDate overlapStart = max(first.get(i).dateFrom(), second.get(j).dateFrom());
            LocalDate overlapEnd = min(first.get(i).dateTo(), second.get(j).dateTo());

            if (!overlapStart.isAfter(overlapEnd)) {
                totalDays += ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
            }

            if (first.get(i).dateTo().isBefore(second.get(j).dateTo())) {
                i++;
            } else {
                j++;
            }
        }

        return totalDays;
    }

    private LocalDate max(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDate min(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
