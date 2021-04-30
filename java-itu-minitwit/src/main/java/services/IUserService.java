package services;

import errorhandling.Result;
import model.dto.*;

import java.util.function.BiFunction;

public interface IUserService {
    Result<String> validateUserCredentials(String username, String email, String password1, String password2);
    Object login(LoginDTO dto);
    void followOrUnfollow(FollowOrUnfollowDTO dto, BiFunction<Integer, String, Result<String>> query, String flashMessage);
    Object getFollow(MessagesPerUserDTO dto);
    Object postFollow(PostFollowDTO dto);
    Object register(RegisterDTO dto);
}
