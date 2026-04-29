package com.simeonatanasov.employees.service;

import com.simeonatanasov.employees.model.WorkRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.simeonatanasov.employees.util.WorkRecordTestFactory.record;
import static org.assertj.core.api.Assertions.assertThat;

class WorkRecordOptimiserTest {

    private final WorkRecordOptimiser optimiser = new WorkRecordOptimiser();

    @Test
    void shouldMergeOverlappingIntervalsForSameEmployeeOnSameProject() {
        List<WorkRecord> records = List.of(
                record(1, 10, "2024-01-01", "2024-01-10"),
                record(1, 10, "2024-01-05", "2024-01-20")
        );

        Map<Long, List<WorkRecord>> result = optimiser.optimizeRecordsPerProject(records);

        assertThat(result.get(10L)).singleElement()
                .satisfies(r -> {
                    assertThat(r.dateFrom()).isEqualTo(LocalDate.parse("2024-01-01"));
                    assertThat(r.dateTo()).isEqualTo(LocalDate.parse("2024-01-20"));
                });
    }

    @Test
    void shouldMergeAdjacentIntervalsForSameEmployeeOnSameProject() {
        List<WorkRecord> records = List.of(
                record(1, 10, "2024-01-01", "2024-01-10"),
                record(1, 10, "2024-01-11", "2024-01-20")
        );

        Map<Long, List<WorkRecord>> result = optimiser.optimizeRecordsPerProject(records);

        assertThat(result.get(10L)).singleElement()
                .satisfies(r -> {
                    assertThat(r.dateFrom()).isEqualTo(LocalDate.parse("2024-01-01"));
                    assertThat(r.dateTo()).isEqualTo(LocalDate.parse("2024-01-20"));
                });
    }

    @Test
    void shouldKeepNonOverlappingIntervalsForSameEmployeeSeparate() {
        List<WorkRecord> records = List.of(
                record(1, 10, "2024-01-01", "2024-01-10"),
                record(1, 10, "2024-01-15", "2024-01-20")
        );

        Map<Long, List<WorkRecord>> result = optimiser.optimizeRecordsPerProject(records);

        assertThat(result.get(10L)).hasSize(2);
    }

    @Test
    void shouldMergeThreeConsecutiveOverlappingIntervalsIntoOne() {
        List<WorkRecord> records = List.of(
                record(1, 10, "2024-01-01", "2024-01-10"),
                record(1, 10, "2024-01-08", "2024-01-15"),
                record(1, 10, "2024-01-12", "2024-01-20")
        );

        Map<Long, List<WorkRecord>> result = optimiser.optimizeRecordsPerProject(records);

        assertThat(result.get(10L)).singleElement()
                .satisfies(r -> {
                    assertThat(r.dateFrom()).isEqualTo(LocalDate.parse("2024-01-01"));
                    assertThat(r.dateTo()).isEqualTo(LocalDate.parse("2024-01-20"));
                });
    }

    @Test
    void shouldMergeIntervalsPerEmployeeIndependently() {
        List<WorkRecord> records = List.of(
                record(1, 10, "2024-01-01", "2024-01-10"),
                record(1, 10, "2024-01-05", "2024-01-15"),
                record(2, 10, "2024-01-01", "2024-01-05"),
                record(2, 10, "2024-01-03", "2024-01-08")
        );

        Map<Long, List<WorkRecord>> result = optimiser.optimizeRecordsPerProject(records);

        List<WorkRecord> projectRecords = result.get(10L);
        assertThat(projectRecords).hasSize(2);
        assertThat(projectRecords).anySatisfy(r -> {
            assertThat(r.employeeId()).isEqualTo(1L);
            assertThat(r.dateFrom()).isEqualTo(LocalDate.parse("2024-01-01"));
            assertThat(r.dateTo()).isEqualTo(LocalDate.parse("2024-01-15"));
        });
        assertThat(projectRecords).anySatisfy(r -> {
            assertThat(r.employeeId()).isEqualTo(2L);
            assertThat(r.dateFrom()).isEqualTo(LocalDate.parse("2024-01-01"));
            assertThat(r.dateTo()).isEqualTo(LocalDate.parse("2024-01-08"));
        });
    }

    @Test
    void shouldNotMergeIntervalsAcrossDifferentProjects() {
        List<WorkRecord> records = List.of(
                record(1, 10, "2024-01-01", "2024-01-10"),
                record(1, 20, "2024-01-05", "2024-01-15")
        );

        Map<Long, List<WorkRecord>> result = optimiser.optimizeRecordsPerProject(records);

        assertThat(result.get(10L)).singleElement()
                .satisfies(r -> assertThat(r.dateTo()).isEqualTo(LocalDate.parse("2024-01-10")));
        assertThat(result.get(20L)).singleElement()
                .satisfies(r -> assertThat(r.dateFrom()).isEqualTo(LocalDate.parse("2024-01-05")));
    }

    @Test
    void shouldGroupRecordsByProject() {
        List<WorkRecord> records = List.of(
                record(1, 10, "2024-01-01", "2024-01-10"),
                record(1, 20, "2024-02-01", "2024-02-10")
        );

        Map<Long, List<WorkRecord>> result = optimiser.optimizeRecordsPerProject(records);

        assertThat(result).containsOnlyKeys(10L, 20L);
    }

}
