package ua.kovalev.recommendation.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "model.initializer")
@Getter
@Setter
public class ModelInitializerProperties {

    private ModelSources source = ModelSources.NETFLIX;

    private Boolean saveAfterBuild = false;

    private Boolean saveAfterUpdate = false;

    /**
     * if train is specified, load only userInteraction matrix and train model
     */
    private Boolean train = true;

    /**
     * if true, netflix dataset is loaded, otherwise table userInteraction is loaded
     */
    private Boolean loadNetflix = false;

    /**
     * Number of item interactions to load from netflix data
     */
    private Integer loadItemCount = -1;

    /**
     *  if specified, ActiveUserDatasetFilter is applied to dataset (compatible only with netflix)
     */
    private Integer userInteractionThreshold = null;

    /**
     * if true, ShrinkUsersDatasetFilter is applied to dataset (compatible only with netflix)
     */
    private Boolean shrinkUsers = null;

    private Tables tables;

    @Getter
    @Setter
    public static class Tables{
        private String userInteraction;
        private String userVector;
        private String itemVector;
    }
}
