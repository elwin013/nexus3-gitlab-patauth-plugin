package com.elwin013.gitlabpatauth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JacksonObjectMapper {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // to have ISO-8601 dates representation
        objectMapper.registerModule(new JavaTimeModule());
    }

    private JacksonObjectMapper() {
    }

    public static ObjectMapper get() {
        return objectMapper;
    }
}
