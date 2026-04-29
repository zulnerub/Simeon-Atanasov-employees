package com.simeonatanasov.employees.controller;

import com.simeonatanasov.employees.dto.ApiErrorResponse;
import com.simeonatanasov.employees.exception.CsvParsingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles CSV parsing and validation failures, returning the list of per-row errors
     * collected during parsing so the client knows exactly which lines were rejected and why.
     */
    @ExceptionHandler(CsvParsingException.class)
    public ResponseEntity<ApiErrorResponse> handleCsvParsingException(CsvParsingException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("CSV validation failed.", ex.getErrors()));
    }

    /**
     * Handles requests that are missing the required multipart file parameter or are not
     * submitted as multipart form data at all.
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, MultipartException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest() {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("Request must contain a CSV file."));
    }

    /**
     * Handles uploads that exceed the configured maximum file size limit
     * (spring.servlet.multipart.max-file-size / max-request-size).
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiErrorResponse("Uploaded file exceeds the maximum allowed size of " + ex.getMaxUploadSize()));
    }

    /**
     * Catch-all for any unhandled exception, preventing stack traces from leaking to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException() {
        return ResponseEntity.internalServerError()
                .body(new ApiErrorResponse("Unexpected server error.", List.of("Please check the input and try again.")));
    }
}
