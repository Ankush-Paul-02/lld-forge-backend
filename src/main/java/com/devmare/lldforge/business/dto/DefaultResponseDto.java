package com.devmare.lldforge.business.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultResponseDto {

    private Status status;
    private Map data;
    private String message;

    public DefaultResponseDto(Status status, String message) {
        this(status, new HashMap<>(), message);
    }

    public enum Status {
        FAILED,
        SUCCESS
    }
}