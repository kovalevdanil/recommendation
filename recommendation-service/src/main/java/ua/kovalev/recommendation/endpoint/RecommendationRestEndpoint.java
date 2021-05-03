package ua.kovalev.recommendation.endpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.kovalev.recommendation.api.RestEndpoint;
import ua.kovalev.recommendation.model.domain.Movie;
import ua.kovalev.recommendation.model.domain.User;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;

import java.util.Arrays;

@RestController
public class RecommendationRestEndpoint implements RestEndpoint{

    @Override
    public ResponseEntity<Response> getRecommendations(Request request) {
        return ResponseEntity.ok(
                Response.builder()
                        .itemCount(10)
                        .items(Arrays.asList(Movie.builder().id(10L).build(), Movie.builder().id(10L).build()))
                        .user(User.builder().id(10L).build())
                    .build()
        );
    }
}
