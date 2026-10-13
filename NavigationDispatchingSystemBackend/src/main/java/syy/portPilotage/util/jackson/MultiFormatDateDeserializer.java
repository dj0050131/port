package syy.portPilotage.util.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class MultiFormatDateDeserializer extends JsonDeserializer<Date> {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter FMT_1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FMT_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FMT_3 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull()) return null;

        if (node.isNumber()) {
            long v = node.asLong();
            if (v > 0 && v < 100000000000L) {
                v = v * 1000L;
            }
            return new Date(v);
        }

        String text = node.asText();
        if (text == null) return null;
        text = text.trim();
        if (text.isEmpty()) return null;

        try {
            return Date.from(Instant.parse(text));
        } catch (DateTimeParseException ignored) {
        }

        try {
            return Date.from(OffsetDateTime.parse(text).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDateTime ldt = LocalDateTime.parse(text, FMT_1);
            return Date.from(ldt.atZone(DEFAULT_ZONE).toInstant());
        } catch (DateTimeParseException ignored) {
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(text, FMT_2);
            return Date.from(ldt.atZone(DEFAULT_ZONE).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDate d = LocalDate.parse(text, FMT_3);
            return Date.from(d.atStartOfDay(DEFAULT_ZONE).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        throw new IOException("Unparseable date: \"" + text + "\"");
    }
}
