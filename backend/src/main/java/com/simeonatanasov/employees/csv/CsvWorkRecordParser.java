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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import static com.simeonatanasov.employees.csv.CsvHeader.*;

@Component
public class CsvWorkRecordParser {

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

    private List<WorkRecord> parse(InputStream inputStream) {
        try (CSVParser csvParser = buildParser(inputStream)) {
            validateHeaders(csvParser.getHeaderMap());

            List<WorkRecord> records = new ArrayList<>();
            List<CsvValidationError> errors = new ArrayList<>();

            for (CSVRecord csvRecord : csvParser) {
                try {
                    records.add(parseRecord(csvRecord));
                } catch (Exception ex) {
                    errors.add(new CsvValidationError(csvRecord.getRecordNumber() + 1, ex.getMessage()));
                }
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

    private CSVParser buildParser(InputStream inputStream) throws IOException {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreSurroundingSpaces(true)
                .setIgnoreHeaderCase(false)
                .build()
                .parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        if (headerMap == null || headerMap.isEmpty()) {
            throw new CsvParsingException("CSV file must contain a header row.");
        }

        List<CsvValidationError> errors = Arrays.stream(CsvHeader.values())
                .map(Enum::name)
                .filter(headerName -> !headerMap.containsKey(headerName))
                .map(headerName -> new CsvValidationError(1, "Missing required header: " + headerName))
                .toList();

        if (!errors.isEmpty()) {
            throw new CsvParsingException(errors);
        }
    }

    private WorkRecord parseRecord(CSVRecord csvRecord) {
        long employeeId = parsePositiveLong(csvRecord.get(EmpID), EmpID);
        long projectId = parsePositiveLong(csvRecord.get(ProjectID), ProjectID);
        LocalDate dateFrom = dateParser.parseDateFrom(csvRecord.get(DateFrom));
        LocalDate dateTo = dateParser.parseDateTo(csvRecord.get(DateTo));
        return new WorkRecord(employeeId, projectId, dateFrom, dateTo);
    }

    private long parsePositiveLong(String value, CsvHeader field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }

        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                throw new IllegalArgumentException(field + " must be positive value.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " must be numeric value.");
        }
    }
}
