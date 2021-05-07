package ua.kovalev.recommendation.endpoint;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import ua.kovalev.recommendation.api.RestEndpoint;
import ua.kovalev.recommendation.exception.NotFoundException;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;
import ua.kovalev.recommendation.service.ModelService;
import ua.kovalev.recommendation.service.RecommendationService;
import ua.kovalev.recommendation.service.UserMappingService;
import ua.kovalev.recommendation.utils.ResponseConverter;

import java.util.List;

@RestController
public class RecommendationRestEndpoint implements RestEndpoint{

    private final EALSModel model;
    private final RecommendationService service;
    private final Validator validator;

    public RecommendationRestEndpoint(EALSModel model, RecommendationService service, @Qualifier("defaultValidator") Validator validator) {
        this.model = model;
        this.service = service;
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

        Response response = service.getRecommendations(model, request);

        if (!response.getSuccess()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
