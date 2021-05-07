package ua.kovalev.recommendation.endpoint;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import ua.kovalev.recommendation.api.RestEndpoint;
import ua.kovalev.recommendation.exception.NotFoundException;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.domain.Movie;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;
import ua.kovalev.recommendation.service.ModelService;
import ua.kovalev.recommendation.service.UserMappingService;
import ua.kovalev.recommendation.utils.ResponseConverter;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RecommendationRestEndpoint implements RestEndpoint{

    private final EALSModel model;
    private final ModelService modelService;
    private final UserMappingService userMappingService;
    private final Validator validator;

    public RecommendationRestEndpoint(EALSModel model, ModelService modelService, UserMappingService userMappingService, @Qualifier("defaultValidator") Validator validator) {
        this.model = model;
        this.modelService = modelService;
        this.userMappingService = userMappingService;
        this.validator = validator;
    }

    @Override
    public ResponseEntity<Response> getRecommendations(@NonNull @RequestBody Request request) {
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);

        if (errors.hasErrors()){
            Response response = ResponseConverter
                    .createResponseFromBadRequest(request, errors);
            return ResponseEntity.badRequest().body(response);
        }

        Integer outerId = request.getUser().getId();

        Integer modelId = userMappingService.getModelId(outerId)
                .orElseThrow(() -> new NotFoundException("Unable to find model id for user " + outerId));

        List<Integer> items = modelService.getRecommendations(model, modelId, request.getItemCount(), false);

        return ResponseEntity.ok(
                ResponseConverter.createSuccessResponse(request, items)
        );
    }
}
