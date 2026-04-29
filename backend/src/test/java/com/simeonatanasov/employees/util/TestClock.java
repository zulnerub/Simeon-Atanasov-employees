package com.simeonatanasov.employees.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class TestClock {

    public static final LocalDate CURRENT_DATE = LocalDate.of(2024, 1, 20);

    public static Clock fixedClock() {
        return Clock.fixed(CURRENT_DATE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    }
}
