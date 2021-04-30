package repository;

import errorhandling.Result;
import model.User;

import java.util.List;

public interface IFollowerRepository {
    Result<Long> countFollowers();
    Result<Boolean> isFollowing(int whoId, int whomId);
    Result<String> followUser(int whoId, String whomUsername);
    Result<List<User>> getFollowing(int whoId);
    Result<String> unfollowUser(int whoId, String whomUsername);
}
