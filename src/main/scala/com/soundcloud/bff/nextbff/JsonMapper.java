package com.soundcloud.bff.nextbff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.soundcloud.jvmkit.module.util.Urn;
import com.soundcloud.publicApiStrangler.support.UrnSerializer;

public class JsonMapper {
    private ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JodaModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .registerModule(new SimpleModule().addSerializer(Urn.class, new UrnSerializer()))
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    public ObjectMapper getMapper() {
        return mapper;
    }
}
