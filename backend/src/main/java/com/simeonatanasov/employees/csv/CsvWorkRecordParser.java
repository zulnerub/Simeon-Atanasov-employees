package com.simeonatanasov.employees.csv;

import com.simeonatanasov.employees.collaboration.WorkRecord;
import com.simeonatanasov.employees.date.FlexibleDateParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CsvWorkRecordParser {

    private static final Set<String> REQUIRED_HEADERS = Set.of("EmpID", "ProjectID", "DateFrom", "DateTo");

    private final FlexibleDateParser dateParser;

    public CsvWorkRecordParser(FlexibleDateParser dateParser) {
        this.dateParser = dateParser;
    }

    public List<WorkRecord> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CsvParsingException("Uploaded file is empty.");
        }

        try {
            return parse(file.getInputStream());
        } catch (IOException ex) {
            throw new CsvParsingException("Could not read uploaded file.");
        }
    }

    public List<WorkRecord> parse(InputStream inputStream) {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser csvParser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreSurroundingSpaces(true)
                     .setIgnoreHeaderCase(false)
                     .build()
                     .parse(reader)) {

            validateHeaders(csvParser.getHeaderMap());

            List<WorkRecord> records = new ArrayList<>();
            List<CsvValidationError> errors = new ArrayList<>();

            for (CSVRecord csvRecord : csvParser) {
                parseRecord(csvRecord, records, errors);
            }

            if (!errors.isEmpty()) {
                throw new CsvParsingException(errors);
            }

            if (records.isEmpty()) {
                throw new CsvParsingException("CSV file does not contain any data rows.");
            }

            return records;
        } catch (IOException ex) {
            throw new CsvParsingException("Could not parse CSV file.");
        }
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        if (headerMap == null || headerMap.isEmpty()) {
            throw new CsvParsingException("CSV file must contain a header row.");
        }

        List<CsvValidationError> errors = REQUIRED_HEADERS.stream()
                .filter(header -> !headerMap.containsKey(header))
                .map(header -> new CsvValidationError(1, "Missing required header: " + header))
                .toList();

        if (!errors.isEmpty()) {
            throw new CsvParsingException(errors);
        }
    }

    private void parseRecord(CSVRecord csvRecord, List<WorkRecord> records, List<CsvValidationError> errors) {
        long lineNumber = csvRecord.getRecordNumber() + 1;

        try {
            long employeeId = parsePositiveLong(csvRecord.get("EmpID"), "EmpID");
            long projectId = parsePositiveLong(csvRecord.get("ProjectID"), "ProjectID");
            LocalDate dateFrom = dateParser.parseDateFrom(csvRecord.get("DateFrom"));
            LocalDate dateTo = dateParser.parseDateTo(csvRecord.get("DateTo"));

            records.add(new WorkRecord(employeeId, projectId, dateFrom, dateTo));
        } catch (Exception ex) {
            errors.add(new CsvValidationError(lineNumber, ex.getMessage()));
        }
    }

    private long parsePositiveLong(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }

        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(fieldName + " must be positive.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be numeric.");
        }
    }
}
