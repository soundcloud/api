package com.soundcloud.publicApiStrangler.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.soundcloud.jvmkit.module.util.Urn;

import java.io.IOException;
import java.util.HashMap;

public class JsonMapper {
    private ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JodaModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true)
            .registerModule(new SimpleModule().addSerializer(Urn.class, new UrnSerializer()))
            .setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

    private ObjectWriter writer = getMapper().writer();

    public ObjectMapper getMapper() {
        return mapper;
    }

    public String toJson(Object o) {
        try {
            return writer.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Could not serialise [%s]", o), e);
        }
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Object> readMap(String json) {
        try {
            return getMapper().readValue(json, HashMap.class);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error parsing JSON:\n###%s\n###", json), e);
        }
    }
}
