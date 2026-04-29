package com.simeonatanasov.employees.controller;

import com.simeonatanasov.employees.dto.AnalyzeResponse;
import com.simeonatanasov.employees.model.WorkRecord;
import com.simeonatanasov.employees.service.CollaborationService;
import com.simeonatanasov.employees.service.CsvWorkRecordParser;
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

    private final CsvWorkRecordParser csvWorkRecordParser;
    private final CollaborationService collaborationService;

    public CollaborationController(CsvWorkRecordParser csvWorkRecordParser, CollaborationService collaborationService) {
        this.csvWorkRecordParser = csvWorkRecordParser;
        this.collaborationService = collaborationService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalyzeResponse analyze(@RequestParam("file") MultipartFile file) {
        List<WorkRecord> workRecords = csvWorkRecordParser.parse(file);
        return collaborationService.findLongestCollaboration(workRecords)
                .map(AnalyzeResponse::from)
                .orElseGet(AnalyzeResponse::empty);
    }
}
