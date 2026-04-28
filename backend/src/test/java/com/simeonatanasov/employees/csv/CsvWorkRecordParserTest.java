package com.simeonatanasov.employees.csv;

import com.simeonatanasov.employees.collaboration.WorkRecord;
import com.simeonatanasov.employees.date.FlexibleDateParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvWorkRecordParserTest {

    private final CsvWorkRecordParser parser = new CsvWorkRecordParser(
            new FlexibleDateParser(Clock.fixed(Instant.parse("2024-01-20T00:00:00Z"), ZoneOffset.UTC))
    );

    @Test
    void shouldParseValidCsv() {
        String csv = """
                EmpID, ProjectID, DateFrom, DateTo
                143, 12, 2013-11-01, 2014-01-05
                218, 10, 2012-05-16, NULL
                """;

        List<WorkRecord> records = parser.parse(stream(csv));

        assertThat(records).hasSize(2);
        assertThat(records.get(0)).isEqualTo(new WorkRecord(
                143,
                12,
                LocalDate.of(2013, 11, 1),
                LocalDate.of(2014, 1, 5)
        ));
        assertThat(records.get(1).dateTo()).isEqualTo(LocalDate.of(2024, 1, 20));
    }

    @Test
    void shouldRejectMissingRequiredHeader() {
        String csv = """
                EmpID, ProjectID, DateFrom
                143, 12, 2013-11-01
                """;

        assertThatThrownBy(() -> parser.parse(stream(csv)))
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

        assertThatThrownBy(() -> parser.parse(stream(csv)))
                .isInstanceOf(CsvParsingException.class)
                .satisfies(ex -> {
                    CsvParsingException csvEx = (CsvParsingException) ex;
                    assertThat(csvEx.getErrors()).hasSize(2);
                    assertThat(csvEx.getErrors().get(0)).contains("Line 2");
                    assertThat(csvEx.getErrors().get(1)).contains("Line 3");
                });
    }

    private ByteArrayInputStream stream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
