package model.dto;

import spark.Request;

import static utilities.Requests.getParam;

public class MessagesPerUserDTO extends DTO {
    public String username;
    public Integer userId;
    public Object flash;

    public static MessagesPerUserDTO fromRequest(Request request) {
        var dto = new MessagesPerUserDTO();
        dto.latest = request.queryParams("latest");
        dto.authorization = request.headers("Authorization");
        dto.username = getParam(":username", request).get();

        return dto;

    }

}
