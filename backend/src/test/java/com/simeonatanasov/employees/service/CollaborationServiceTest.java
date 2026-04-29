package com.simeonatanasov.employees.service;

import com.simeonatanasov.employees.model.CollaborationResult;
import com.simeonatanasov.employees.model.EmployeePair;
import com.simeonatanasov.employees.util.TestClock;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CollaborationServiceTest {

    private final FlexibleDateParser dateParser = new FlexibleDateParser(TestClock.fixedClock());
    private final CsvWorkRecordParser csvParser = new CsvWorkRecordParser(dateParser);

    private final WorkRecordOptimiser optimiser = new WorkRecordOptimiser();
    private final ProjectOverlapCalculator overlapCalculator = new ProjectOverlapCalculator(optimiser);

    private final CollaborationService service = new CollaborationService(csvParser, overlapCalculator);

    @Test
    void shouldReturnLongestCollaboratingPair() {
        String csv = """
                EmpID,ProjectID,DateFrom,DateTo
                143,10,2024-01-01,2024-01-10
                218,10,2024-01-05,2024-01-10
                143,20,2024-02-01,2024-02-10
                218,20,2024-02-01,2024-02-05
                300,20,2024-02-01,2024-02-02
                """;

        CollaborationResult result = service.analyze(multipartFile(csv)).orElseThrow();

        assertThat(result.pair()).isEqualTo(new EmployeePair(143, 218));
        assertThat(result.totalDaysWorked()).isEqualTo(11);
    }

    @Test
    void shouldReturnEmptyWhenNoEmployeesOverlap() {
        String csv = """
                EmpID,ProjectID,DateFrom,DateTo
                143,10,2024-01-01,2024-01-10
                218,10,2024-01-11,2024-01-20
                """;

        Optional<CollaborationResult> result = service.analyze(multipartFile(csv));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldTreatNullDateToAsCurrentDate() {
        String csv = """
                EmpID,ProjectID,DateFrom,DateTo
                143,10,2024-01-01,NULL
                218,10,2024-01-15,NULL
                """;

        CollaborationResult result = service.analyze(multipartFile(csv)).orElseThrow();

        assertThat(result.pair()).isEqualTo(new EmployeePair(143, 218));
        assertThat(result.totalDaysWorked()).isEqualTo(6);
    }

    @Test
    void shouldReturnEmptyWhenOnlyOneEmployee() {
        String csv = """
                EmpID,ProjectID,DateFrom,DateTo
                143,10,2024-01-01,2024-01-10
                """;

        Optional<CollaborationResult> result = service.analyze(multipartFile(csv));

        assertThat(result).isEmpty();
    }

    private MockMultipartFile multipartFile(String csv) {
        return new MockMultipartFile("file", csv.getBytes(StandardCharsets.UTF_8));
    }
}
