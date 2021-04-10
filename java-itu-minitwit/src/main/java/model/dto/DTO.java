package model.dto;

import spark.Request;
import spark.Response;

public class DTO {
    public String latest;           //request.queryParams("latest")
    public String authorization;    //request.headers("Authorization")
    public Request request;
    public Response response;

}
