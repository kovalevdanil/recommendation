package ua.kovalev.recommendation.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.kovalev.recommendation.model.domain.Item;
import ua.kovalev.recommendation.model.domain.User;


import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private User user;
    private List<Item> items;
    private Boolean excludeInteracted;

    private Boolean success;
    private String errorDescription;
}
