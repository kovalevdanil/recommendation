package ua.kovalev.recommendation.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTechData implements Serializable {
    private String responseCode;
    private Boolean success;
    private Boolean fromCache;
    private UUID correlationId;
    private String errorDescription;
}
