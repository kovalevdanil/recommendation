package ua.kovalev.recommendation.mf.data;

import lombok.*;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Rating {
    private int userId;
    private int itemId;
}
