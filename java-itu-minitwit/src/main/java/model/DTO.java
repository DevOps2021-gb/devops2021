package model;

import errorhandling.Result;
import spark.Request;
import spark.Response;

public class DTO {
    public String latest;           //request.queryParams("latest")
    public String authorization;    //request.headers("Authorization")
    public String username;
    public String content;
    public Result<String> follow;
    public Result<String> unfollow;
    public Request request;
    public Response response;
}
