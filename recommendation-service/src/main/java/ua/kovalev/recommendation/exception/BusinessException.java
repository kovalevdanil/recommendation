package ua.kovalev.recommendation.exception;


public class BusinessException extends RuntimeException{
    public BusinessException(String message){
        super(message);
    }

    public BusinessException(){}
}
