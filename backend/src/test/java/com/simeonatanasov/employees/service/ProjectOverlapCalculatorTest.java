package com.simeonatanasov.employees.service;

import com.simeonatanasov.employees.model.EmployeePair;
import com.simeonatanasov.employees.model.ProjectOverlap;
import com.simeonatanasov.employees.model.WorkRecord;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.simeonatanasov.employees.util.WorkRecordTestFactory.record;
import static org.assertj.core.api.Assertions.assertThat;

class ProjectOverlapCalculatorTest {

    private final ProjectOverlapCalculator calculator = new ProjectOverlapCalculator(new WorkRecordOptimiser());

    @Test
    void shouldCalculateOverlapDaysForTwoEmployeesOnSameProject() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-10"),
                record(218, 10, "2024-01-05", "2024-01-10")
        );

        List<ProjectOverlap> overlaps = calculator.findOverlaps(records);

        assertThat(overlaps).singleElement()
                .satisfies(o -> {
                    assertThat(new EmployeePair(o.employeeId1(), o.employeeId2())).isEqualTo(new EmployeePair(143, 218));
                    assertThat(o.projectId()).isEqualTo(10L);
                    assertThat(o.daysWorked()).isEqualTo(6L);
                });
    }

    @Test
    void shouldReturnEmptyWhenEmployeesDoNotOverlap() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-10"),
                record(218, 10, "2024-01-11", "2024-01-20")
        );

        List<ProjectOverlap> overlaps = calculator.findOverlaps(records);

        assertThat(overlaps).isEmpty();
    }

    @Test
    void shouldCountSameDayOverlapAsOneDay() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-01"),
                record(218, 10, "2024-01-01", "2024-01-01")
        );

        List<ProjectOverlap> overlaps = calculator.findOverlaps(records);

        assertThat(overlaps).singleElement()
                .extracting(ProjectOverlap::daysWorked)
                .isEqualTo(1L);
    }

    @Test
    void shouldNormalizePairSoLowerEmployeeIdComesFirst() {
        List<WorkRecord> records = List.of(
                record(218, 10, "2024-01-01", "2024-01-10"),
                record(143, 10, "2024-01-01", "2024-01-10")
        );

        List<ProjectOverlap> overlaps = calculator.findOverlaps(records);

        assertThat(overlaps).singleElement()
                .satisfies(o -> {
                    assertThat(o.employeeId1()).isEqualTo(143L);
                    assertThat(o.employeeId2()).isEqualTo(218L);
                });
    }

    @Test
    void shouldProduceOneOverlapPerProjectPerPair() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-10"),
                record(218, 10, "2024-01-05", "2024-01-10"),
                record(143, 20, "2024-02-01", "2024-02-10"),
                record(218, 20, "2024-02-01", "2024-02-05")
        );

        List<ProjectOverlap> overlaps = calculator.findOverlaps(records);

        assertThat(overlaps)
                .extracting(ProjectOverlap::projectId, ProjectOverlap::daysWorked)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(10L, 6L),
                        Tuple.tuple(20L, 5L)
                );
    }

    @Test
    void shouldFindAllPairsWhenThreeEmployeesWorkOnSameProject() {
        List<WorkRecord> records = List.of(
                record(1, 10, "2024-01-01", "2024-01-10"),
                record(2, 10, "2024-01-01", "2024-01-10"),
                record(3, 10, "2024-01-01", "2024-01-10")
        );

        List<ProjectOverlap> overlaps = calculator.findOverlaps(records);

        assertThat(overlaps).hasSize(3);
        assertThat(overlaps)
                .extracting(o -> new EmployeePair(o.employeeId1(), o.employeeId2()))
                .containsExactlyInAnyOrder(
                        new EmployeePair(1, 2),
                        new EmployeePair(1, 3),
                        new EmployeePair(2, 3)
                );
    }

    @Test
    void shouldReturnEmptyWhenOnlyOneEmployeeOnProject() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-10")
        );

        List<ProjectOverlap> overlaps = calculator.findOverlaps(records);

        assertThat(overlaps).isEmpty();
    }

    @Test
    void shouldOnlyCountOverlapWithinMatchingIntervalSegment() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-05"),
                record(143, 10, "2024-01-15", "2024-01-20"),
                record(218, 10, "2024-01-03", "2024-01-05")
        );

        List<ProjectOverlap> overlaps = calculator.findOverlaps(records);

        assertThat(overlaps).singleElement()
                .extracting(ProjectOverlap::daysWorked)
                .isEqualTo(3L);
    }

    @Test
    void shouldNotDoubleCountWhenEmployeeHasOverlappingRecordsOnSameProject() {
        List<WorkRecord> records = List.of(
                record(143, 10, "2024-01-01", "2024-01-10"),
                record(143, 10, "2024-01-05", "2024-01-20"),
                record(218, 10, "2024-01-08", "2024-01-15")
        );

        List<ProjectOverlap> overlaps = calculator.findOverlaps(records);

        assertThat(overlaps).singleElement()
                .extracting(ProjectOverlap::daysWorked)
                .isEqualTo(8L);
    }

}
