package com.simeonatanasov.employees.service;

import com.simeonatanasov.employees.model.CollaborationResult;
import com.simeonatanasov.employees.model.EmployeePair;
import com.simeonatanasov.employees.model.ProjectOverlap;
import com.simeonatanasov.employees.model.WorkInterval;
import com.simeonatanasov.employees.model.WorkRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CollaborationService {

    public Optional<CollaborationResult> findLongestCollaboration(List<WorkRecord> records) {
        if (records == null || records.isEmpty()) {
            return Optional.empty();
        }

        Map<Long, Map<Long, List<WorkInterval>>> intervalsByProjectAndEmployee = records.stream()
                .collect(Collectors.groupingBy(
                        WorkRecord::projectId,
                        Collectors.groupingBy(
                                WorkRecord::employeeId,
                                Collectors.mapping(
                                        record -> new WorkInterval(record.dateFrom(), record.dateTo()),
                                        Collectors.collectingAndThen(Collectors.toList(), this::mergeIntervals)
                                )
                        )
                ));

        Map<EmployeePair, PairAccumulator> accumulators = new HashMap<>();

        for (Map.Entry<Long, Map<Long, List<WorkInterval>>> projectEntry : intervalsByProjectAndEmployee.entrySet()) {
            long projectId = projectEntry.getKey();
            Map<Long, List<WorkInterval>> intervalsByEmployee = projectEntry.getValue();
            List<Long> employeeIds = intervalsByEmployee.keySet().stream().sorted().toList();

            for (int i = 0; i < employeeIds.size(); i++) {
                for (int j = i + 1; j < employeeIds.size(); j++) {
                    long firstEmployeeId = employeeIds.get(i);
                    long secondEmployeeId = employeeIds.get(j);
                    long projectOverlapDays = calculateTotalOverlapDays(
                            intervalsByEmployee.get(firstEmployeeId),
                            intervalsByEmployee.get(secondEmployeeId)
                    );

                    if (projectOverlapDays > 0) {
                        EmployeePair pair = new EmployeePair(firstEmployeeId, secondEmployeeId);
                        accumulators
                                .computeIfAbsent(pair, PairAccumulator::new)
                                .addProjectOverlap(projectId, projectOverlapDays);
                    }
                }
            }
        }

        return accumulators.values()
                .stream()
                .map(PairAccumulator::toResult)
                .max(Comparator
                        .comparingLong(CollaborationResult::totalDaysWorked)
                        .thenComparing(result -> result.pair().employeeId1(), Comparator.reverseOrder())
                        .thenComparing(result -> result.pair().employeeId2(), Comparator.reverseOrder()))
                .map(this::sortProjectOverlaps);
    }

    private CollaborationResult sortProjectOverlaps(CollaborationResult result) {
        List<ProjectOverlap> sortedProjects = result.projects()
                .stream()
                .sorted(Comparator
                        .comparingLong(ProjectOverlap::projectId)
                        .thenComparingLong(ProjectOverlap::daysWorked))
                .toList();

        return new CollaborationResult(result.totalDaysWorked(), result.pair(), sortedProjects);
    }

    private List<WorkInterval> mergeIntervals(List<WorkInterval> intervals) {
        if (intervals.isEmpty()) {
            return List.of();
        }

        List<WorkInterval> sortedIntervals = intervals.stream()
                .sorted(Comparator.comparing(WorkInterval::start))
                .toList();

        List<WorkInterval> merged = new ArrayList<>();
        WorkInterval current = sortedIntervals.get(0);

        for (int i = 1; i < sortedIntervals.size(); i++) {
            WorkInterval next = sortedIntervals.get(i);
            if (current.overlapsOrTouches(next)) {
                current = current.merge(next);
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return merged;
    }

    private long calculateTotalOverlapDays(List<WorkInterval> firstIntervals, List<WorkInterval> secondIntervals) {
        long totalDays = 0;
        int firstIndex = 0;
        int secondIndex = 0;

        while (firstIndex < firstIntervals.size() && secondIndex < secondIntervals.size()) {
            WorkInterval first = firstIntervals.get(firstIndex);
            WorkInterval second = secondIntervals.get(secondIndex);

            LocalDate overlapStart = max(first.start(), second.start());
            LocalDate overlapEnd = min(first.end(), second.end());

            if (!overlapStart.isAfter(overlapEnd)) {
                totalDays += ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
            }

            if (first.end().isBefore(second.end())) {
                firstIndex++;
            } else {
                secondIndex++;
            }
        }

        return totalDays;
    }

    private LocalDate max(LocalDate first, LocalDate second) {
        return first.isAfter(second) ? first : second;
    }

    private LocalDate min(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }

    private static final class PairAccumulator {
        private final EmployeePair pair;
        private final Map<Long, Long> daysByProject = new LinkedHashMap<>();

        private PairAccumulator(EmployeePair pair) {
            this.pair = pair;
        }

        void addProjectOverlap(long projectId, long daysWorked) {
            daysByProject.merge(projectId, daysWorked, Long::sum);
        }

        CollaborationResult toResult() {
            List<ProjectOverlap> projects = daysByProject.entrySet()
                    .stream()
                    .map(entry -> new ProjectOverlap(
                            pair.employeeId1(),
                            pair.employeeId2(),
                            entry.getKey(),
                            entry.getValue()
                    ))
                    .toList();

            long totalDaysWorked = projects.stream()
                    .mapToLong(ProjectOverlap::daysWorked)
                    .sum();

            return new CollaborationResult(totalDaysWorked, pair, projects);
        }
    }
}
