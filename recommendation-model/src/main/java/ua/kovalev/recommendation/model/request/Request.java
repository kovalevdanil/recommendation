package ua.kovalev.recommendation.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.kovalev.recommendation.model.domain.User;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @NotNull
    private User user;

    @Min(1)
    private Integer itemCount;

    private Boolean excludeInteracted;
}
