package com.devmare.lldforge.business.converter;

import com.devmare.lldforge.data.enums.Topic;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class TopicListConverter implements AttributeConverter<List<Topic>, String> {

    @Override
    public String convertToDatabaseColumn(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) return "";
        return topics.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    @Override
    public List<Topic> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return List.of();
        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(Topic::valueOf)
                .collect(Collectors.toList());
    }
}
