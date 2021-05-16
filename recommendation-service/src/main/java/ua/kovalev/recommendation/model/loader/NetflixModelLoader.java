package ua.kovalev.recommendation.model.loader;

import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.DatasetConstants;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;
import ua.kovalev.recommendation.mf.filter.ActiveUsersDatasetFilter;
import ua.kovalev.recommendation.mf.filter.DatasetFilter;
import ua.kovalev.recommendation.mf.filter.ShrinkUsersDatasetFilter;
import ua.kovalev.recommendation.mf.reader.NetflixDatasetLoader;
import ua.kovalev.recommendation.mf.util.DatasetUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NetflixModelLoader implements ModelLoader{


    private ModelInitializerProperties props;

    public NetflixModelLoader(ModelInitializerProperties props){
        this.props = props;
    }

    @Override
    public EALSModel load(Map<String, Object> config) {
        List<DatasetFilter> filters = new ArrayList<>();

        if (props.getUserInteractionThreshold() != null){
            int threshold = Optional.ofNullable(props.getUserInteractionThreshold()).orElse(-1);
            filters.add(new ActiveUsersDatasetFilter(threshold));
        }

        if (props.getShrinkUsers()){
            filters.add(new ShrinkUsersDatasetFilter());
        }

        NetflixDatasetLoader reader = new NetflixDatasetLoader(DatasetConstants.NETLFIX_DATASET, filters);

        Dataset dataset;
        try {
            dataset = reader.load(props.getLoadItemCount());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to load the model");
        }

        SparseRealMatrix matrix = DatasetUtils.buildDatasetMatrix(dataset);
        return new EALSModel(matrix, config);
    }
}
