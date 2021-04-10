package controllers;

import spark.Request;
import spark.Response;

public class ResReqSparkWrapper {
    public final Request req;
    public final Response res;
    public final boolean isGet;

    public ResReqSparkWrapper(Request req, Response res, boolean isGet) {
        this.req = req;
        this.res = res;
        this.isGet = isGet;
    }
}
