package ua.kovalev.recommendation.utils;

import lombok.experimental.UtilityClass;
import org.springframework.validation.Errors;
import ua.kovalev.recommendation.model.domain.Item;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;
import ua.kovalev.recommendation.model.response.ResponseBusinessData;
import ua.kovalev.recommendation.model.response.ResponseTechData;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ResponseConverter {

    public static Response createResponseFromBadRequest(Request request, Errors errors){
        String errorDescription = errors == null ? "" : errors.getFieldErrors().stream()
                .map(e -> MessageFormat.format("{0} - {1}", e.getField(),
                        e.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        return Response.builder()
                .techData(ResponseTechData.builder()
                        .correlationId(request.getTechData().getCorrelationId())
                        .errorDescription(errorDescription)
                        .success(false)
                        .build())
                .build();
    }

    public static Response createResponseWithErrorDescription(Request request, String errorDescription){
        return Response.builder()
            .techData(ResponseTechData.builder()
                    .success(false)
                    .errorDescription(errorDescription)
                    .correlationId(request.getTechData().getCorrelationId())
                    .build())
                .build();
    }

    public static Response createSuccessResponse(Request request, List<Integer> items){
        return Response.builder()
                .businessData(ResponseBusinessData.builder()
                        .excludeInteracted(request.getBusinessData().getExcludeInteracted())
                        .items(items.stream().map(Item::new).collect(Collectors.toList()))
                        .user(request.getBusinessData().getUser())
                        .build())
                .techData(ResponseTechData.builder()
                        .correlationId(request.getTechData().getCorrelationId())
                        .fromCache(!request.getTechData().getDisableCache())
                        .success(true)
                        .build())
                .build();
    }

}
