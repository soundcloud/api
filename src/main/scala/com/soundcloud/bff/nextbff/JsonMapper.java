package com.soundcloud.bff.nextbff;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.soundcloud.jvmkit.module.util.Urn;
import com.soundcloud.publicApiStrangler.support.UrnSerializer;

import java.io.IOException;
import java.util.HashMap;

public class JsonMapper {
    private ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JodaModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true)
            .registerModule(new SimpleModule().addSerializer(Urn.class, new UrnSerializer()))
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    public ObjectMapper getMapper() {
        return mapper;
    }
}
