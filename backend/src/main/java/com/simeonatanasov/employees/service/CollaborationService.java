package com.simeonatanasov.employees.service;

import com.simeonatanasov.employees.model.CollaborationResult;
import com.simeonatanasov.employees.model.EmployeePair;
import com.simeonatanasov.employees.model.ProjectOverlap;
import com.simeonatanasov.employees.model.WorkRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

@Service
public class CollaborationService {

    private final CsvWorkRecordParser csvWorkRecordParser;
    private final ProjectOverlapCalculator projectOverlapCalculator;

    public CollaborationService(CsvWorkRecordParser csvWorkRecordParser, ProjectOverlapCalculator projectOverlapCalculator) {
        this.csvWorkRecordParser = csvWorkRecordParser;
        this.projectOverlapCalculator = projectOverlapCalculator;
    }

    public Optional<CollaborationResult> analyze(MultipartFile file) {
        List<WorkRecord> records = csvWorkRecordParser.parse(file);
        List<ProjectOverlap> overlaps = projectOverlapCalculator.findOverlaps(records);
        return buildResult(overlaps);
    }

    private Optional<CollaborationResult> buildResult(List<ProjectOverlap> overlaps) {
        return overlaps.stream()
                .collect(groupingBy(overlap -> new EmployeePair(overlap.employeeId1(), overlap.employeeId2())))
                .entrySet().stream()
                .map(this::toCollaborationResult)
                .max(getComparator());
    }

    /**
     * Returns a comparator that selects the pair with the most total days worked together.
     * When two pairs have equal total days worked, then its compared by first employee id,
     * if there is a record that both first criteria match tehn it compares by second employId
     * (not possible to have a match here if the previous two criteria were matching)
     * providing a deterministic result.
     */
    private Comparator<CollaborationResult> getComparator() {
        return Comparator
                .comparingLong(CollaborationResult::totalDaysWorked)
                .thenComparing(r -> r.pair().employeeId1(), Comparator.reverseOrder())
                .thenComparing(r -> r.pair().employeeId2(), Comparator.reverseOrder());
    }

    private CollaborationResult toCollaborationResult(Map.Entry<EmployeePair, List<ProjectOverlap>> overlapEntry) {
        List<ProjectOverlap> overlaps = overlapEntry.getValue();
        long totalDays = overlaps.stream().mapToLong(ProjectOverlap::daysWorked).sum();
        return new CollaborationResult(totalDays, overlapEntry.getKey(), overlaps);
    }
}
