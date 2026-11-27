package ch.heigvd.amt.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.heigvd.amt.presentation.ConcertDto;

/** Jackson lit et écrit un record sans annotation. JSON n'a pas de type pour les dates. */
class JsonTest {

    static final ConcertDto CONCERT = new ConcertDto(
            1L, "Zaho de Sagazan", "Grande scène", LocalDateTime.of(2027, 7, 9, 21, 0));

    @Test
    void byDefaultADateIsWrittenAsAnArray() throws JsonProcessingException {
        var mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        assertEquals("""
                {"id":1,"artist":"Zaho de Sagazan","stage":"Grande scène",\
                "startsAt":[2027,7,9,21,0]}""", mapper.writeValueAsString(CONCERT));
    }

    @Test
    void aConfiguredMapperWritesIso8601AndReadsItBack() throws JsonProcessingException {
        var mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String json = mapper.writeValueAsString(CONCERT);

        assertEquals("""
                {"id":1,"artist":"Zaho de Sagazan","stage":"Grande scène",\
                "startsAt":"2027-07-09T21:00:00"}""", json);
        assertEquals(CONCERT, mapper.readValue(json, ConcertDto.class));
    }
}
