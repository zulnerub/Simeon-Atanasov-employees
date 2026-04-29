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

    @ExceptionHandler(CsvParsingException.class)
    public ResponseEntity<ApiErrorResponse> handleCsvParsingException(CsvParsingException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("CSV validation failed.", ex.getErrors()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MultipartException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("Request must contain a CSV file in the 'file' multipart field."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiErrorResponse("Uploaded file exceeds the maximum allowed size."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(new ApiErrorResponse("Unexpected server error.", List.of("Please check the input and try again.")));
    }
}
