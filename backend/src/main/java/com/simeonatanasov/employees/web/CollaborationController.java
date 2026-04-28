package com.simeonatanasov.employees.web;

import com.simeonatanasov.employees.collaboration.CollaborationResult;
import com.simeonatanasov.employees.collaboration.CollaborationService;
import com.simeonatanasov.employees.collaboration.WorkRecord;
import com.simeonatanasov.employees.csv.CsvWorkRecordParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/collaborations")
public class CollaborationController {

    private final CsvWorkRecordParser parser;
    private final CollaborationService service;

    public CollaborationController(CsvWorkRecordParser parser, CollaborationService service) {
        this.parser = parser;
        this.service = service;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalyzeResponse analyze(@RequestParam("file") MultipartFile file) {
        List<WorkRecord> records = parser.parse(file);
        return service.findLongestCollaboration(records)
                .map(AnalyzeResponse::from)
                .orElseGet(AnalyzeResponse::empty);
    }
}
