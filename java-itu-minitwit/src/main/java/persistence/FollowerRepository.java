package persistence;

import model.Follower;
import model.User;
import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;

import java.util.List;

public class FollowerRepository {

    private FollowerRepository() {}

    public static Result<Long> countFollowers() {
        var db = DB.connectDb().get();
        var result = db.sql("select count(*) from follower").first(Long.class);
        return new Success<>(result);
    }

    public static Result<Boolean> isFollowing(int whoId, int whomId) {
        try {
            var db = DB.connectDb().get();
            var result = db.where("whoId=?", whoId).where("whomId=?", whomId).results(Follower.class);
            return new Success<>(!result.isEmpty());
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    public static Result<String> followUser(int whoId, String whomUsername) {
        Result<User> whoUser = UserRepository.getUserById(whoId);
        Result<Integer> whomId = UserRepository.getUserId(whomUsername);

        if (!whoUser.isSuccess()) {
            return new Failure<>(whoUser.toString());
        } else if (!whomId.isSuccess()) {
            return new Failure<>(whomId.toString());
        } else {
            try {
                var db = DB.connectDb().get();
                db.insert(new Follower(whoId, whomId.get()));

                return new Success<>("OK");
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }
    }

    public static Result<List<User>> getFollowing(int whoId) {
        try{
            var db = DB.connectDb().get();

            List<User> result = db.sql(
                    "select user.* from user " +
                            "inner join follower on follower.whomId=user.id " +
                            "where follower.whoId=? "+
                            "limit ?", whoId, MessageRepository.PER_PAGE).results(User.class);
            return new Success<>(result);
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    public static Result<String> unfollowUser(int whoId, String whomUsername) {
        Result<User> whoUser = UserRepository.getUserById(whoId);
        Result<Integer> whomId = UserRepository.getUserId(whomUsername);

        if (!whoUser.isSuccess()) {
            return new Failure<>(whoUser.toString());
        } else if (!whomId.isSuccess()) {
            return new Failure<>(whomId.toString());
        } else {
            try {
                var db = DB.connectDb().get();
                db.table("follower").where("whoId=?", whoId).where("whomId=?", whomId.get()).delete();

                return new Success<>("OK");
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }
    }
}
