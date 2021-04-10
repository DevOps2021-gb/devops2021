package model.dto;

import errorhandling.Result;

public class PostFollowDTO extends DTO {
    public String username;
    public Result<String> follow;
    public Result<String> unfollow;
}
