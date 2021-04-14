package model.dto;

import spark.Request;

import static services.MessageService.USERNAME;
import static utilities.Requests.getFromBody;
import static utilities.Requests.getSessionUserId;

public class FollowOrUnfollowDTO extends UserDTO{
    public String profileUsername;

    public static FollowOrUnfollowDTO fromRequest(Request request) {
        var dto = new FollowOrUnfollowDTO();
        dto.latest = request.queryParams("latest");
        dto.userId = getSessionUserId();

        var params = getFromBody(request, USERNAME);
        dto.profileUsername = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");

        return dto;
    }

}
