package ua.kovalev.recommendation.model.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class Key {
    private String key;
}
