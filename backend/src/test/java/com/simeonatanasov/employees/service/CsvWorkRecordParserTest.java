package com.simeonatanasov.employees.service;

import com.simeonatanasov.employees.exception.CsvParsingException;
import com.simeonatanasov.employees.model.WorkRecord;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvWorkRecordParserTest {

    private final LocalDate CURRENT_DATE = LocalDate.parse("2024-01-20");

    private final CsvWorkRecordParser parser = new CsvWorkRecordParser(
            new FlexibleDateParser(Clock.fixed(Instant.from(CURRENT_DATE), ZoneOffset.UTC))
    );

    @Test
    void shouldParseValidCsv() {
        String csv = """
                EmpID, ProjectID, DateFrom, DateTo
                143, 12, 2013-11-01, 2014-01-05
                218, 10, 2012-05-16, NULL
                """;

        List<WorkRecord> records = parser.parse(multipartFile(csv));

        assertThat(records).hasSize(2);
        assertThat(records.get(0)).isEqualTo(new WorkRecord(
                143,
                12,
                LocalDate.of(2013, 11, 1),
                LocalDate.of(2014, 1, 5)
        ));
        assertThat(records.get(1).dateTo()).isEqualTo(CURRENT_DATE);
    }

    @Test
    void shouldRejectMissingRequiredHeader() {
        String csv = """
                EmpID, ProjectID, DateFrom
                143, 12, 2013-11-01
                """;

        assertThatThrownBy(() -> parser.parse(multipartFile(csv)))
                .isInstanceOf(CsvParsingException.class)
                .hasMessageContaining("CSV validation failed");
    }

    @Test
    void shouldCollectValidationErrors() {
        String csv = """
                EmpID, ProjectID, DateFrom, DateTo
                abc, 12, 2013-11-01, 2014-01-05
                218, 10, 2024-01-20, 2024-01-01
                """;

        assertThatThrownBy(() -> parser.parse(multipartFile(csv)))
                .isInstanceOf(CsvParsingException.class)
                .satisfies(ex -> {
                    CsvParsingException csvEx = (CsvParsingException) ex;
                    assertThat(csvEx.getErrors()).hasSize(2);
                    assertThat(csvEx.getErrors().get(0)).contains("Line 2");
                    assertThat(csvEx.getErrors().get(1)).contains("Line 3");
                });
    }

    private MockMultipartFile multipartFile(String csv) {
        return new MockMultipartFile("file", csv.getBytes(StandardCharsets.UTF_8));
    }
}
