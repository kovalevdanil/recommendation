package ua.kovalev.recommendation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface RestEndpoint {

    @ResponseBody
    @RequestMapping(
            value = "/recommendations",
            method = RequestMethod.POST,
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE
    )
    ResponseEntity<Response> recommendations(@RequestBody Request request);
}
