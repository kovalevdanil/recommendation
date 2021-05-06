package ua.kovalev.recommendation.endpoint;

import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.kovalev.recommendation.api.RestEndpoint;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.domain.Movie;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;
import ua.kovalev.recommendation.service.ItemMappingService;
import ua.kovalev.recommendation.service.ModelService;
import ua.kovalev.recommendation.service.UserMappingService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class RecommendationRestEndpoint implements RestEndpoint{

    private final EALSModel model;
    private final ModelService modelService;
    private final UserMappingService userMappingService;
    private final ItemMappingService itemMappingService;

    public RecommendationRestEndpoint(EALSModel model, ModelService modelService, UserMappingService userMappingService, ItemMappingService itemMappingService) {
        this.model = model;
        this.modelService = modelService;
        this.userMappingService = userMappingService;
        this.itemMappingService = itemMappingService;
    }

    @Override
    public ResponseEntity<Response> getRecommendations(@NonNull @RequestBody Request request) {
        Objects.requireNonNull(request.getUser());
        Objects.requireNonNull(request.getUser().getId());
        Integer outerId = request.getUser().getId();

        Integer modelId = userMappingService.getModelId(outerId)
                .orElseThrow(() -> new RuntimeException("Unable to find model id for user"));

        List<Integer> items = modelService.getRecommendations(model, modelId, false);

        return ResponseEntity.ok(Response.builder()
                .user(request.getUser())
                .items(items.stream().map(Movie::new).collect(Collectors.toList()))
                .itemCount(items.size())
                .build()
        );
    }
}
