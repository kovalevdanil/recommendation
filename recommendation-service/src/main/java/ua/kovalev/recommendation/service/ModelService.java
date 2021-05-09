package ua.kovalev.recommendation.service;


import ua.kovalev.recommendation.model.request.Request;
import ua.kovalev.recommendation.model.response.Response;

public interface ModelService {
    boolean update(Integer u, Integer i);
    void build();
    Integer addUser(Integer id);
    Integer addItem(Integer id);
    Response recommendations(Request request);
}
