package ua.kovalev.recommendation.utils;

public class AssertUtils {
    public static void requireTrue(boolean exp, String message){
        if (!exp) {
            throw new RuntimeException(message);
        }
    }
    public static void requireTrue(boolean exp){
        if (!exp) {
            throw new RuntimeException();
        }
    }
}
