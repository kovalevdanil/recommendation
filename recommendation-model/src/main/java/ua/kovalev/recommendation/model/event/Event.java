package ua.kovalev.recommendation.model.event;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String sender;
    private LocalDate timestamp;
    private EventType type;

    private JsonNode data;
}
