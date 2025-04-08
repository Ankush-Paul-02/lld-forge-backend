package com.devmare.lldforge.business.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UpdateMentorApplicationRequestDto {

    @NotBlank(message = "Application id is required!")
    private String id;

    @NotBlank(message = "Application status is required!")
    private String status;

    private String reason;
}
