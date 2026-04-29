package com.simeonatanasov.employees.util;

import com.simeonatanasov.employees.model.WorkRecord;

import java.time.LocalDate;

public class WorkRecordTestFactory {

    public static WorkRecord record(long employeeId, long projectId, String from, String to) {
        return new WorkRecord(employeeId, projectId, LocalDate.parse(from), LocalDate.parse(to));
    }
}
