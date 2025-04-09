package com.devmare.lldforge.business.dto;

import com.devmare.lldforge.data.entity.Topic;
import com.devmare.lldforge.data.enums.Difficulty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequestDto {

    @NotBlank(message = "Title is required.")
    private String title;

    @NotBlank(message = "Description is required.")
    @Size(max = 16000, message = "Description must not exceed 16000 characters.")
    private String description;

    @NotNull(message = "At least one topic is required.")
    private List<Topic> topics;

    @NotBlank(message = "Constraints is required.")
    private String constraints;

    private String expectedEntitiesDescription;

    @NotNull(message = "Difficulty must be specified.")
    private Difficulty difficulty;
}
