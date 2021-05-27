package ua.kovalev.recommendation.model.event.data;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ItemCreatedData {
    private Integer id;
}
