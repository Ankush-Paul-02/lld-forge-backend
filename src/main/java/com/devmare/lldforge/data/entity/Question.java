package com.devmare.lldforge.data.entity;

import com.devmare.lldforge.data.enums.Difficulty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collation = "questions")
public class Question {

    @Id
    private String id;

    private String title;

    private String description;

    private List<Topic> topics;

    private String constraints;

    private String expectedEntitiesDescription;

    private String authorId;

    private Long postedAt;

    private Difficulty difficulty;
}
