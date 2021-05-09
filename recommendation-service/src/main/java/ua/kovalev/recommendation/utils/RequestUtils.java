package ua.kovalev.recommendation.utils;

import lombok.experimental.UtilityClass;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.request.RequestTechData;

import java.util.UUID;

@UtilityClass
public class RequestUtils {

    public static void normalizeRequest(Request request){
        if (request.getTechData() == null) {
            request.setTechData(new RequestTechData());
        }
        normalizeRequestTechData(request.getTechData());
    }

    public static void normalizeRequestTechData(RequestTechData techData){
        if (techData.getCorrelationId() == null) {
            techData.setCorrelationId(UUID.randomUUID());
        }
        if (techData.getDisableCache() == null){
            techData.setDisableCache(false);
        }
    }

}
