package ua.kovalev.recommendation.mf.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dataset {
    private List<Interaction> interactions;
    private int userCount;
    private int itemCount;
}
