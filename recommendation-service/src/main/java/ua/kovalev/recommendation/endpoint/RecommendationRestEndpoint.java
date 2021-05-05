package ua.kovalev.recommendation.endpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.kovalev.recommendation.api.RestEndpoint;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;
import ua.kovalev.recommendation.service.ModelService;

@RestController
public class RecommendationRestEndpoint implements RestEndpoint{

    private final EALSModel model;
    private final ModelService modelService;

    public RecommendationRestEndpoint(EALSModel model, ModelService modelService) {
        this.model = model;
        this.modelService = modelService;
    }

    @Override
    public ResponseEntity<Response> getRecommendations(Request request) {
        modelService.dumpModel(model);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/interact")
    public ResponseEntity<?> interaction(@RequestParam int user, @RequestParam int item){
        model.updateModel(user, item);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/build")
    public void build(){
        model.buildModel();
    }
}
