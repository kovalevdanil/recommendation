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
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;
import ua.kovalev.recommendation.service.ModelService;
import ua.kovalev.recommendation.utils.RequestUtils;
import ua.kovalev.recommendation.utils.ResponseConverter;

@RestController
public class RecommendationRestEndpoint implements RestEndpoint{

    private final Validator validator;
    private final ModelService service;

    public RecommendationRestEndpoint(ModelService service, @Qualifier("defaultValidator") Validator validator) {
        this.service = service;
        this.validator = validator;
    }

    @Override
    public ResponseEntity<Response> getRecommendations(@NonNull @RequestBody Request request) {
        RequestUtils.normalizeRequest(request);

        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);
        if (errors.hasErrors()){
            Response response = ResponseConverter
                    .createResponseFromBadRequest(request, errors);
            return ResponseEntity.badRequest().body(response);
        }

        Response response = service.recommendations(request);

        if (!response.getTechData().getSuccess()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
