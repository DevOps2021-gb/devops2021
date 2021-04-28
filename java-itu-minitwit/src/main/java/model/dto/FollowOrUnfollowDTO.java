package model.dto;

import spark.Request;
import utilities.IRequests;

import static services.MessageService.USERNAME;

public class FollowOrUnfollowDTO extends UserDTO{
    public String profileUsername;

    public static FollowOrUnfollowDTO fromRequest(Request request, IRequests requests) {
        var dto = new FollowOrUnfollowDTO();
        dto.latest = request.queryParams("latest");
        dto.userId = requests.getSessionUserId();

        var params = requests.getFromBody(request, USERNAME).get();
        dto.profileUsername = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");

        return dto;
    }

}
