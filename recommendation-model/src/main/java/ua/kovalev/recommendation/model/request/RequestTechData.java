package ua.kovalev.recommendation.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestTechData {
    private UUID correlationId;
    private Boolean disableCacheReads = false;
    private Boolean disableCacheWrites = false;
}
