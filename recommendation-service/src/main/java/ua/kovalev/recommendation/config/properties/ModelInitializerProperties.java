package ua.kovalev.recommendation.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "model.initializer")
@Getter
@Setter
public class ModelInitializerProperties {

    /**
     * Where to look for model params
     */
    private ModelSources source = ModelSources.NETFLIX;

    /**
     * If persisting in database is needed after build (will overwrite existing data including mappings)
     */
    private Boolean saveAfterBuild = false;

    private Boolean saveAfterUpdate = false;

    private Boolean saveItemIdPool = true;

    private Boolean saveUserIdPool = false;

    /**
     * if train is specified, load only userInteraction matrix and train model
     */
    private Boolean train = false;

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
