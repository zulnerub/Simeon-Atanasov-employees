package com.simeonatanasov.employees.service;

import com.simeonatanasov.employees.model.WorkRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

/**
 * Merges overlapping or adjacent work records so that each employee has at most one
 * record per continuous period on a given project.
 *
 * <p>Processing flow:
 * <ol>
 *   <li>Group all records by project.</li>
 *   <li>Within each project, group records by employee.</li>
 *   <li>Sort each employee's records by start date and iterate over them, extending
 *       the current interval whenever the next record overlaps or touches it, and
 *       finalising it when a gap is found into a single record.</li>
 * </ol>
 *
 * <p>The result represents all records of all employees optimised per each project.
 */
@Component
public class WorkRecordOptimiser {

    /**
     * Groups the input by project and delegates per-project merging.
     *
     * @param records work records, may contain overlapping intervals
     * @return merged records grouped by project ID without overlaping records.
     */
    public Map<Long, List<WorkRecord>> optimizeRecordsPerProject(List<WorkRecord> records) {
        return records.stream()
                .collect(groupingBy(WorkRecord::projectId))
                .entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> optimizeEmployeeRecords(entry.getValue())));
    }

    /**
     * Groups a project's records by employee and merges each employee's records that are overlapping.
     */
    private List<WorkRecord> optimizeEmployeeRecords(List<WorkRecord> projectRecords) {
        return projectRecords.stream()
                .collect(groupingBy(WorkRecord::employeeId))
                .values().stream()
                .flatMap(employeeRecords -> mergeEmployeeRecords(employeeRecords).stream())
                .toList();
    }

    /**
     * Merges a single employee's records by comparing each with the next after they have been sorted by start date.
     * The current record is extended whenever
     * the next record overlaps or is starts on the next day of the previous ending, and finalised when a gap appears.
     * The running record is tracked in {@code currentRecord}, which acts as an
     * accumulator: each merge produces a new record whose end date covers both
     * the current record and the next, so three or more consecutive overlapping
     * records are reduced to a single record.
     */
    private List<WorkRecord> mergeEmployeeRecords(List<WorkRecord> records) {
        List<WorkRecord> recordsByStartDate = records.stream()
                .sorted(Comparator.comparing(WorkRecord::dateFrom))
                .toList();

        List<WorkRecord> mergedEmployeeRecords = new ArrayList<>();
        WorkRecord currentRecord = recordsByStartDate.getFirst();

        for (int i = 1; i < recordsByStartDate.size(); i++) {
            WorkRecord nextRecord = recordsByStartDate.get(i);
            if (areRecordsOverlapping(currentRecord, nextRecord)) {
                currentRecord = new WorkRecord(
                        currentRecord.employeeId(),
                        currentRecord.projectId(),
                        currentRecord.dateFrom(),
                        determineMergedRecordEndDate(currentRecord, nextRecord)
                );
            } else {
                mergedEmployeeRecords.add(currentRecord);
                currentRecord = nextRecord;
            }
        }

        mergedEmployeeRecords.add(currentRecord);
        return mergedEmployeeRecords;
    }

    private LocalDate determineMergedRecordEndDate(WorkRecord currentRecord, WorkRecord nextRecord) {
        return currentRecord.dateTo().isAfter(nextRecord.dateTo()) ? currentRecord.dateTo() : nextRecord.dateTo();
    }

    private boolean areRecordsOverlapping(WorkRecord currentRecord, WorkRecord nextRecord) {
        return !currentRecord.dateTo().plusDays(1).isBefore(nextRecord.dateFrom());
    }
}
