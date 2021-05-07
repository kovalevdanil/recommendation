package ua.kovalev.recommendation.utils;

import lombok.experimental.UtilityClass;
import org.springframework.validation.Errors;
import ua.kovalev.recommendation.model.domain.Item;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;

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
                .errorDescription(errorDescription)
                .success(false)
                .build();
    }

    public static Response createSuccessResponse(Request request, List<Integer> items){
        return Response.builder()
                .items(items.stream().map(Item::new).collect(Collectors.toList()))
                .success(true)
                .excludeInteracted(request.getExcludeInteracted())
                .user(request.getUser())
                .build();
    }

}
