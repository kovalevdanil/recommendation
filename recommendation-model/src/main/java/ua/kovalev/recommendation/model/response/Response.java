package ua.kovalev.recommendation.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.kovalev.recommendation.model.domain.Movie;
import ua.kovalev.recommendation.model.domain.User;


import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private User user;
    private List<Movie> items;
    private Integer itemCount;
}
