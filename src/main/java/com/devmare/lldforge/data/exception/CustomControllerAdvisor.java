package com.devmare.lldforge.data.exception;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.FAILED;

@ControllerAdvice
public class CustomControllerAdvisor {

    @ExceptionHandler(AppInfoException.class)
    public ResponseEntity<DefaultResponseDto> handleAppInfoException(AppInfoException ex) {
        DefaultResponseDto response = new DefaultResponseDto(DefaultResponseDto.Status.FAILED, ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DefaultResponseDto> handleGenericException(Exception ex) {
        DefaultResponseDto response = new DefaultResponseDto(FAILED, "An unexpected error occurred.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
