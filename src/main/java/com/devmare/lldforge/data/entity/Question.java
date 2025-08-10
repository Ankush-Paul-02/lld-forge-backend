package com.devmare.lldforge.data.entity;

import com.devmare.lldforge.business.converter.TopicListConverter;
import com.devmare.lldforge.data.enums.Difficulty;
import com.devmare.lldforge.data.enums.Topic;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions")
public class Question extends BaseEntity {

    private String title;

    private String description;

    @Convert(converter = TopicListConverter.class)
    private List<Topic> topics;

    private String constraints;

    private String expectedEntitiesDescription;

    @ManyToOne
    private User author;

    private Long postedAt;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
}
