package ua.kovalev.recommendation.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.kovalev.recommendation.model.domain.Item;
import ua.kovalev.recommendation.model.domain.User;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseBusinessData implements Serializable {
    private User user;
    private List<Item> items;
    private Boolean excludeInteracted;
}
