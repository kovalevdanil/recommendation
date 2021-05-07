package ua.kovalev.recommendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kovalev.recommendation.exception.NotFoundException;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.domain.User;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;
import ua.kovalev.recommendation.utils.ResponseConverter;

import java.util.List;
import java.util.Optional;

@Service
public class RecommendationService {

    @Autowired
    UserMappingService userMappingService;

    @Autowired
    ModelService modelService;

    public Response getRecommendations(EALSModel model, Request request){
        Integer outerId = request.getUser().getId();

        Optional<Integer> modelIdOptional = userMappingService.getModelId(outerId);

        if (modelIdOptional.isEmpty()){
            return Response.builder()
                    .success(false)
                    .errorDescription("No user with id " + outerId + " found")
                    .build();
        }

        List<Integer> items = modelService.getRecommendations(model, modelIdOptional.get(), request.getItemCount(), false);

        return ResponseConverter.createSuccessResponse(request, items);
    }

}
