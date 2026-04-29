package com.simeonatanasov.employees.date;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlexibleDateParserTest {

    private final LocalDate CURRENT_DATE = LocalDate.parse("2024-01-20");

    private final FlexibleDateParser parser = new FlexibleDateParser(
            Clock.fixed(Instant.from(CURRENT_DATE), ZoneOffset.UTC)
    );

    @Test
    void shouldParseSupportedDateFormats() {
        List<String> values = List.of(
                "2024-01-20",
                "2024/01/20",
                "2024.01.20",
                "20-01-2024",
                "20/01/2024",
                "20.01.2024",
                "01/20/2024",
                "Jan 20, 2024",
                "January 20, 2024",
                "20 Jan 2024",
                "20 January 2024",
                "2024-01-20T10:15:30"
        );

        for (String value : values) {
            assertThat(parser.parseDateFrom(value)).isEqualTo(CURRENT_DATE);
        }
    }

    @Test
    void shouldTreatNullDateToAsToday() {
        assertThat(parser.parseDateTo("NULL")).isEqualTo(CURRENT_DATE);
        assertThat(parser.parseDateTo("null")).isEqualTo(CURRENT_DATE);
        assertThat(parser.parseDateTo(" ")).isEqualTo(CURRENT_DATE);
    }

    @Test
    void shouldRejectUnrecognizedDateFormat() {
        assertThatThrownBy(() -> parser.parseDateFrom("2024|01|20"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unrecognized date format");
    }

    @Test
    void shouldRejectInvalidDate() {
        assertThatThrownBy(() -> parser.parseDateFrom("31-31-2024"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date provided");
    }
}
