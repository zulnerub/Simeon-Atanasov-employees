package com.simeonatanasov.employees.service;

import com.simeonatanasov.employees.model.CollaborationResult;
import com.simeonatanasov.employees.model.EmployeePair;
import com.simeonatanasov.employees.model.ProjectOverlap;
import com.simeonatanasov.employees.model.WorkRecord;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CollaborationServiceTest {

    private final CollaborationService service = new CollaborationService();

    @Test
    void shouldFindLongestCollaborationAcrossMultipleProjects() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-10"),
                record(218, 10, "2024-01-05", "2024-01-10"),
                record(143, 20, "2024-02-01", "2024-02-10"),
                record(218, 20, "2024-02-01", "2024-02-05"),
                record(300, 20, "2024-02-01", "2024-02-02")
        );

        CollaborationResult result = service.findLongestCollaboration(records).orElseThrow();

        assertThat(result.pair()).isEqualTo(new EmployeePair(143, 218));
        assertThat(result.totalDaysWorked()).isEqualTo(11);
        assertThat(result.projects())
                .extracting(ProjectOverlap::projectId, ProjectOverlap::daysWorked)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(10L, 6L),
                        Tuple.tuple(20L, 5L)
                );
    }

    @Test
    void shouldReturnEmptyWhenThereIsNoOverlap() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-10"),
                record(218, 10, "2024-01-11", "2024-01-20")
        );

        Optional<CollaborationResult> result = service.findLongestCollaboration(records);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCountSameDayOverlapAsOneDay() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-01"),
                record(218, 10, "2024-01-01", "2024-01-01")
        );

        CollaborationResult result = service.findLongestCollaboration(records).orElseThrow();

        assertThat(result.totalDaysWorked()).isEqualTo(1);
        assertThat(result.projects()).singleElement()
                .extracting(ProjectOverlap::daysWorked)
                .isEqualTo(1L);
    }

    @Test
    void shouldNormalizeEmployeePairOrder() {
        List<WorkRecord> records = List.of(
                record(218, 10, "2024-01-01", "2024-01-10"),
                record(143, 10, "2024-01-01", "2024-01-10")
        );

        CollaborationResult result = service.findLongestCollaboration(records).orElseThrow();

        assertThat(result.pair()).isEqualTo(new EmployeePair(143, 218));
    }

    @Test
    void shouldMergeOverlappingIntervalsForSameEmployeeAndProjectToAvoidDoubleCounting() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-10"),
                record(143, 10, "2024-01-05", "2024-01-20"),
                record(218, 10, "2024-01-08", "2024-01-15")
        );

        CollaborationResult result = service.findLongestCollaboration(records).orElseThrow();

        assertThat(result.totalDaysWorked()).isEqualTo(8);
        assertThat(result.projects()).singleElement()
                .extracting(ProjectOverlap::daysWorked)
                .isEqualTo(8L);
    }

    private WorkRecord record(long employeeId, long projectId, String from, String to) {
        return new WorkRecord(employeeId, projectId, LocalDate.parse(from), LocalDate.parse(to));
    }
}
