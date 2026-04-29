package com.simeonatanasov.employees.date;

import org.springframework.stereotype.Component;

import java.text.ParsePosition;
import java.time.Clock;
import java.time.LocalDate;
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
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
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

        return formatters.stream()
                .filter(formatter -> findFormatter(formatter, value))
                .findFirst()
                .map(formatter -> parseLocalDate(rawValue, formatter, value))
                .orElseThrow(() -> new IllegalArgumentException("Unrecognized date format: " + rawValue));
    }

    private LocalDate parseLocalDate(String rawValue, DateTimeFormatter formatter, String value) {
        try {
            return LocalDate.parse(value, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date provided: " + rawValue);
        }
    }

    private boolean findFormatter(DateTimeFormatter formatter, String value) {
        ParsePosition pos = new ParsePosition(0);
        return formatter.parseUnresolved(value, pos) != null && pos.getIndex() == value.length();
    }

    private DateTimeFormatter formatter(String pattern) {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH);
    }
}
