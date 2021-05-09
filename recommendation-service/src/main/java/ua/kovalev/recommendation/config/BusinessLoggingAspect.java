package ua.kovalev.recommendation.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.response.Response;
import ua.kovalev.recommendation.service.ModelService;

import java.util.Arrays;
import java.util.stream.Collectors;

import static ua.kovalev.recommendation.utils.LoggingConstants.*;

@Aspect
@Configuration
@Slf4j
public class BusinessLoggingAspect {

    @Around("blOperations()")
    public Object before(ProceedingJoinPoint joinPoint) throws Throwable {

        String args = Arrays.stream(joinPoint.getArgs()).map(Object::toString).collect(Collectors.joining("; "));
        String methodName = joinPoint.getStaticPart().getSignature().getName();

        log.info("Service method {}", methodName);

        MDC.put(ACTION_NAME, methodName);
        MDC.put(ARGUMENTS, args);

        log.info(STARTED);

        return joinPoint.proceed();
    }

    @AfterReturning(value = "blOperations()", returning = "result")
    public void afterReturning(Object result){
        if (result != null) {
            MDC.put(RESULT, result.toString());
        }

        if (result instanceof Response){
            boolean success = ((Response) result).getSuccess();
            log.info(success ? COMPLETED : FAILED);
        }

        MDC.clear();
    }

    @AfterThrowing(value = "blOperations()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Throwable exception){

        if (exception instanceof Exception){
            String errorDescription = (exception).getMessage();
            MDC.put(ERROR_DESCRIPTION, errorDescription);
        }

        log.info(FAILED);
        MDC.clear();
    }

    @Pointcut("execution (* ua.kovalev.recommendation.service.ModelService.*(..))")
    private void blOperations(){}


//    @Pointcut("execution(* ua.kovalev.recommendation.mf.algorithm.als.EALSModel.buildModel(..))")
//    private void buildModelOperation(){}
//
//    @Pointcut("execution(void ua.kovalev.recommendation.mf.algorithm.als.EALSModel.updateModel(..))")
//    private void updateModelOperation(){}
//
//    @Pointcut("execution(int ua.kovalev.recommendation.mf.algorithm.als.EALSModel.addUser(..))")
//    private void addUserOperation(){}
//
//    @Pointcut("execution(int ua.kovalev.recommendation.mf.algorithm.als.EALSModel.addItem(..))")
//    private void addItemOperation(){}
}
