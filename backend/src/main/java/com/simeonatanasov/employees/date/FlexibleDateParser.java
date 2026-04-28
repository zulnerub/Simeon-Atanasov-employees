package com.simeonatanasov.employees.date;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Component
public class FlexibleDateParser {

    private final Clock clock;
    private final List<DateTimeFormatter> formatters;

    public FlexibleDateParser(Clock clock) {
        this.clock = clock;
        this.formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                formatter("uuuu/MM/dd"),
                formatter("uuuu.MM.dd"),
                formatter("dd-MM-uuuu"),
                formatter("dd/MM/uuuu"),
                formatter("dd.MM.uuuu"),
                formatter("MM/dd/uuuu"),
                formatter("MMM d, uuuu"),
                formatter("MMMM d, uuuu"),
                formatter("d MMM uuuu"),
                formatter("d MMMM uuuu")
        );
    }

    public LocalDate parseDateFrom(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DateFrom is required.");
        }
        return parse(value);
    }

    public LocalDate parseDateTo(String value) {
        if (value == null || value.isBlank() || value.trim().equalsIgnoreCase("NULL")) {
            return LocalDate.now(clock);
        }
        return parse(value);
    }

    private LocalDate parse(String rawValue) {
        String value = rawValue.trim();

        if (value.contains("T")) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
            } catch (DateTimeParseException ignored) {
            }
        }

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("Unsupported date format: " + rawValue);
    }

    private DateTimeFormatter formatter(String pattern) {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH);
    }
}
