package ua.kovalev.recommendation.model.response;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseCodes {
    public static final String OK = "ok"; // Запрос обработан успешно
    public static final String USER_NOT_FOUND = "user_not_found"; // Указанный пользователь не был найден
    public static final String INTERNAL_SERVER_ERROR = "internal_server_error"; // Внутрення ошибка сервера
    public static final String BAD_REQUEST = "bad_request"; // Некорректный запрос
}
