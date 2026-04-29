package com.simeonatanasov.employees.controller;

import com.simeonatanasov.employees.dto.AnalyzeResponse;
import com.simeonatanasov.employees.service.CollaborationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/collaborations")
public class CollaborationController {

    private final CollaborationService collaborationService;

    public CollaborationController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalyzeResponse analyze(@RequestParam("file") MultipartFile file) {
        return collaborationService.analyze(file)
                .map(AnalyzeResponse::from)
                .orElseGet(AnalyzeResponse::empty);
    }
}
