package ua.kovalev.recommendation.model.event.data;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class InteractionData {
    private Integer userId;
    private Integer itemId;
}
