package ua.kovalev.recommendation.endpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ua.kovalev.recommendation.api.RestEndpoint;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;

@RestController
public class RecommendationRestEndpoint implements RestEndpoint{

    private final EALSModel model;

    public RecommendationRestEndpoint(EALSModel model) {
        this.model = model;
    }

    @Override
    public ResponseEntity<Response> getRecommendations(Request request) {
        model.updateModel(0, 0);

        return ResponseEntity.ok().build();
    }
}
