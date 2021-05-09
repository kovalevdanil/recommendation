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
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;
import ua.kovalev.recommendation.service.ModelService;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ua.kovalev.recommendation.utils.LoggingConstants.*;

@Aspect
@Configuration
@Slf4j(topic = "businessOperationLogger")
public class BusinessLoggingAspect {

    @Around("blOperations()")
    public Object before(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getStaticPart().getSignature().getName();

        log.info("Service method invoked '{}'", methodName);

        MDC.put(ACTION_NAME, methodName);
        Object[] args = joinPoint.getArgs();

        if (args != null && args.length > 0){
            CodeSignature signature = (CodeSignature)  joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();

            String argsString = normalizeArguments(IntStream.range(0, args.length)
                    .mapToObj(i -> String.format("%s=%s", parameterNames[i], args[i]))
                    .collect(Collectors.joining("; "))
            );

            MDC.put(ARGUMENTS, argsString);

            if (args[0] instanceof Request){
                Request request = (Request) args[0];
                if (request.getTechData() != null) {
                    MDC.put(CORRELATION_ID, request.getTechData().getCorrelationId().toString());
                }
            } else {
                MDC.put(CORRELATION_ID, UUID.randomUUID().toString());
            }
        }

        log.info(STARTED);

        return joinPoint.proceed();
    }

    @AfterReturning(value = "blOperations()", returning = "result")
    public void afterReturning(Object result){
        if (result != null) {
            MDC.put(RESULT, result.toString());
        }

        if (result instanceof Response){
            boolean success = ((Response) result).getTechData().getSuccess();
            log.info(success ? COMPLETED : FAILED);
        } else {
            log.info(COMPLETED);
        }

        MDC.clear();
    }

    @AfterThrowing(value = "blOperations()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Throwable exception){

        String errorDescription = exception.getMessage();
        MDC.put(ERROR_DESCRIPTION, errorDescription);

        log.info(FAILED);
        MDC.clear();
    }

    @Pointcut("execution (* ua.kovalev.recommendation.service.ModelService.*(..))")
    private void blOperations(){}

    private String normalizeArguments(String args){
        return "("  + args + ")";
    }
}
