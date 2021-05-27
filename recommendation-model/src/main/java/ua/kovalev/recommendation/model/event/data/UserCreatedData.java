package ua.kovalev.recommendation.model.event.data;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserCreatedData {
    private Integer id;
}
