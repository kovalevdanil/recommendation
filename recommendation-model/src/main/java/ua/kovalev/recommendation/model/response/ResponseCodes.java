package ua.kovalev.recommendation.model.response;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseCodes {
    public static final String OK = "ok";
    public static final String USER_NOT_FOUND = "user_not_found";
    public static final String INTERNAL_SERVER_ERROR = "internal_server_error";
    public static final String BAD_REQUEST = "bad_request";
}
