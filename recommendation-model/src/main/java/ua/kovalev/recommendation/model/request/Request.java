package ua.kovalev.recommendation.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.kovalev.recommendation.model.domain.User;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @NotNull
    @Valid
    private RequestBusinessData businessData;

    private RequestTechData techData;
}
