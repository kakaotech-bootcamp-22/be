package com.spring.be.exception;

import com.spring.be.config.FileUploadConfig;
import org.apache.coyote.Response;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버에서 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
    }

    // HttpClientErrorException 처리
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleHttpClientErrorException(HttpClientErrorException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body("HTTP 요청 중 오류가 발생했습니다: " + ex.getMessage());
    }

    // RestTemplate 관련 예외 처리 (일반적인 예외)
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<String> handleRestClientException(RestClientException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("외부 API 호출 중 오류가 발생했습니다: " + ex.getMessage());
    }

    // 파일 업로드 중 오류 처리
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleFileUploadException(MultipartException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("파일 업로드 중 오류가 발생했습니다: " + ex.getMessage());
    }

    // 파일 크기 초과 예외 처리
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("파일 크기가 너무 큽니다. 제한 크기를 확인하세요." + ex.getMessage());
    }


}
