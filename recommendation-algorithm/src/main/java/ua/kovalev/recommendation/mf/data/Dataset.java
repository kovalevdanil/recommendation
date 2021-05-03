package ua.kovalev.recommendation.mf.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {
    private List<Rating> ratings;
    private int userCount;
    private int itemCount;
}
