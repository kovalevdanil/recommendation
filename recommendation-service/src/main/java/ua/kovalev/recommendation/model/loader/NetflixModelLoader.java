package ua.kovalev.recommendation.model.loader;

import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.DatasetConstants;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;
import ua.kovalev.recommendation.mf.filter.ActiveUsersDatasetFilter;
import ua.kovalev.recommendation.mf.filter.DatasetFilter;
import ua.kovalev.recommendation.mf.filter.ShrinkUsersDatasetFilter;
import ua.kovalev.recommendation.mf.reader.NetflixRatingReader;
import ua.kovalev.recommendation.mf.util.DatasetUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NetflixModelLoader implements ModelLoader{


    private ModelInitializerProperties props;
    private Map<String, Object> modelConfig;

    public NetflixModelLoader(ModelInitializerProperties props, Map<String, Object> modelConfig){
        this.props = props;
        this.modelConfig = modelConfig;
    }

    @Override
    public EALSModel load() {
        List<DatasetFilter> filters = new ArrayList<>();

        if (props.getUserInteractionThreshold() != null){
            filters.add(new ActiveUsersDatasetFilter(props.getUserInteractionThreshold()));
        }

        if (props.getShrinkUsers()){
            filters.add(new ShrinkUsersDatasetFilter());
        }

        NetflixRatingReader reader = new NetflixRatingReader(DatasetConstants.NETLFIX_DATASET, filters);

        Dataset dataset = null;

        try {
            dataset = reader.read(props.getLoadItemCount());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to load the model");
        }

        SparseRealMatrix matrix = DatasetUtils.buildDatasetMatrix(dataset);

        return new EALSModel(matrix, modelConfig);
    }
}
