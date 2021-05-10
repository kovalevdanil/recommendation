package ua.kovalev.recommendation.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ua.kovalev.recommendation.config.properties.ModelConfig;
import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
import ua.kovalev.recommendation.exception.BusinessException;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.loader.ModelLoader;
import ua.kovalev.recommendation.model.loader.ModelLoaderFactory;
import ua.kovalev.recommendation.model.repository.ItemRepository;
import ua.kovalev.recommendation.model.repository.ModelRepository;
import ua.kovalev.recommendation.model.repository.UserRepository;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;
import ua.kovalev.recommendation.model.response.ResponseCodes;
import ua.kovalev.recommendation.utils.LoggingConstants;
import ua.kovalev.recommendation.utils.ResponseConverter;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ModelServiceImpl implements ModelService {

    private final Integer OUTER_ID_NOT_FOUND_DEFAULT = -1;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    EALSModel model;

    @Autowired
    ModelInitializerProperties initProps;

    @Autowired
    ModelLoaderFactory modelLoaderFactory;

    @Autowired
    @Qualifier("modelConfig")
    ModelConfig config;

    @PostConstruct
    public void init(){
        ModelLoader loader = modelLoaderFactory.getModelLoader(initProps.getSource());
        model = loader.load(config.getConfig());

        if (initProps.getTrain()){
            this.build();
        }
    }

    @Override
    public boolean update(Integer u, Integer i) {
        Integer modelUserId = userRepository.findModelId(u)
                .orElseThrow(() -> new BusinessException("Mapping for user " + u + " wasn't found"));

        Integer modelItemId = itemRepository.findModelId(i)
                .orElseThrow(() -> new BusinessException("Mapping for item " + i + " wasn't found"));

        model.updateModel(modelUserId, modelItemId);

        return modelRepository.update(model, u, i);
    }

    @Override
    public void build() {
        model.buildModel();

        if (initProps.getSaveAfterBuild()){
            modelRepository.save(model);

            if (initProps.getSaveItemIdPool()){
                int itemPool = model.getItemCount();
                itemRepository.saveIdPool(itemPool);
            }

            if (initProps.getSaveUserIdPool()){
                int userPool = model.getUserCount();
                userRepository.saveIdPool(userPool);
            }
        }
    }

    @Override
    public Integer addUser(Integer id) {
        if (userRepository.existsByOuterId(id)){
            throw new BusinessException("User with id " + id + " already exists");
        }

        int modelId = model.addUser();
        userRepository.save(id, modelId);
        modelRepository.saveUser(model, modelId);

        return modelId;
    }

    @Override
    public Integer addItem(Integer id) {
        if (itemRepository.existsByOuterId(id)){
            throw new BusinessException("Item with id " + id + " already exists");
        }

        int modelId = model.addItem();
        itemRepository.save(id, modelId);
        modelRepository.saveItem(model, modelId);

        return modelId;
    }

    @Override
    @Cacheable(value = "recommendations", key = "#request.businessData.user.id", unless = "#request.techData.disableCache")
    public Response recommendations(Request request) {
        Integer outerId = request.getBusinessData().getUser().getId();

        Optional<Integer> modelIdOptional = userRepository.findModelId(outerId);

        if (modelIdOptional.isEmpty()){
            return ResponseConverter
                    .createResponseWithErrorDescription(request, ResponseCodes.USER_NOT_FOUND,"User with id " + outerId + " not found");
        }

        List<Integer> items = model
                .getRecommendations(modelIdOptional.get(), request.getBusinessData().getItemCount(),  request.getBusinessData().getExcludeInteracted())
                .stream().map(item -> itemRepository.findOuterId(item).orElse(OUTER_ID_NOT_FOUND_DEFAULT))
                .filter(item -> !OUTER_ID_NOT_FOUND_DEFAULT.equals(item))
                .collect(Collectors.toList());

        MDC.put(LoggingConstants.DATA_FROM_CACHE, "false");
        return ResponseConverter.createSuccessResponse(request, items);
    }
}
