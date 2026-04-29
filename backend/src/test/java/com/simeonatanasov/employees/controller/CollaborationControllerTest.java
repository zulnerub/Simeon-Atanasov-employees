package com.simeonatanasov.employees.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CollaborationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAnalyzeUploadedCsv() throws Exception {
        String csv = """
                EmpID,ProjectID,DateFrom,DateTo
                143,10,2024-01-01,2024-01-10
                218,10,2024-01-05,2024-01-10
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/collaborations/analyze").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId1").value(143))
                .andExpect(jsonPath("$.employeeId2").value(218))
                .andExpect(jsonPath("$.totalDaysWorked").value(6))
                .andExpect(jsonPath("$.projects[0].projectId").value(10))
                .andExpect(jsonPath("$.projects[0].daysWorked").value(6));
    }

    @Test
    void shouldReturnBadRequestForInvalidCsv() throws Exception {
        String csv = """
                EmpID,ProjectID,DateFrom,DateTo
                abc,10,2024-01-01,2024-01-10
                """;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/collaborations/analyze").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CSV validation failed."))
                .andExpect(jsonPath("$.errors[0]").value(containsString("EmpID must be numeric")));
    }
}
