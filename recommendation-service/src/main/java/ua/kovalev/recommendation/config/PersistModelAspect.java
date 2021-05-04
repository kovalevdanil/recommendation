package ua.kovalev.recommendation.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;

@Aspect
@Configuration
@Slf4j
public class PersistModelAspect {

    @Value("${model.initializer.save-after-build:false}")
    private Boolean saveAfterBuild;

    @Value("${mode.initializer.save-after-update:false}")
    private Boolean saveAfterUpdate;

    @Autowired(required = false)
    private JdbcTemplate template;


    @Around(value = "buildModelOperation()")
    public Object afterModelBuild(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    @Around(value = "updateModelOperation()")
    public Object afterModelUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        if (saveAfterUpdate){
            Object[] arguments = joinPoint.getArgs();

            int u = (Integer) arguments[0];
            int i = (Integer) arguments[1];

            EALSModel model = (EALSModel) joinPoint.getThis();

            double[] userVector = model.getU().getRowRef(u);
            double[] itemVector = model.getV().getRowRef(i);
        }
        return joinPoint.proceed();
    }

    @Pointcut("execution(void ua.kovalev.recommendation.mf.algorithm.als.EALSModel.buildModel(..))")
    private void buildModelOperation(){}

    @Pointcut("execution(void ua.kovalev.recommendation.mf.algorithm.als.EALSModel.updateModel(..))")
    private void updateModelOperation(){}
}
