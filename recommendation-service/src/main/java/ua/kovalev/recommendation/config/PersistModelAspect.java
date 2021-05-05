package ua.kovalev.recommendation.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.service.ModelService;

@Aspect
@Configuration
@Slf4j
public class PersistModelAspect {

    @Value("${model.initializer.save-after-build:false}")
    private Boolean saveAfterBuild;

    @Value("${model.initializer.save-after-update:false}")
    private Boolean saveAfterUpdate;

    @Autowired
    private ModelService modelService;


    @Around(value = "buildModelOperation()")
    public Object afterModelBuild(ProceedingJoinPoint joinPoint) throws Throwable {
        if (saveAfterBuild){
            log.info("Persisting model into database...");

            EALSModel model = (EALSModel) joinPoint.getThis();
            try {
                modelService.dumpModel(model);
            } catch (Exception ex){
                log.error(ex.getMessage());
            }
        }

        return joinPoint.proceed();
    }

    @Around(value = "updateModelOperation()")
    public Object afterModelUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        if (saveAfterUpdate){

            Object[] arguments = joinPoint.getArgs();

            Integer u = (Integer) arguments[0];
            Integer i = (Integer) arguments[1];

            MDC.put("User ID", u.toString());
            MDC.put("Item ID ", i.toString());

            log.info("Persisting model update");

            EALSModel model = (EALSModel) joinPoint.getThis();

            try {
                modelService.persistUserInteraction(model, u, i);
                log.info("Persisting success");
            } catch (Exception ex){
                log.error(ex.getMessage());
            }

            MDC.clear();
        }

        return joinPoint.proceed();
    }

    @Pointcut("execution(void ua.kovalev.recommendation.mf.algorithm.als.EALSModel.buildModel(..))")
    private void buildModelOperation(){}

    @Pointcut("execution(void ua.kovalev.recommendation.mf.algorithm.als.EALSModel.updateModel(..))")
    private void updateModelOperation(){}
}