package ua.kovalev.recommendation.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "model")
@Getter
@Setter
public class ModelProperties {
    private Double regularizationParameter;
    private Integer factors;
    private Integer offlineIterations;
    private Integer onlineIterations;
    private Double latentInitMean;
    private Double latentInitDeviation;
    private Double popularitySignificance;
    private Double missingDataWeight;
    private Double newItemWeight;
}
