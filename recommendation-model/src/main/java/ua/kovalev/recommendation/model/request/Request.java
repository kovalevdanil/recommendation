package ua.kovalev.recommendation.model.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.kovalev.recommendation.model.domain.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private User user;
    private Integer itemCount;
}
