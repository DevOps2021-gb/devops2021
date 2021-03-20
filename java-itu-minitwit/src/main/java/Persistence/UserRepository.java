package Persistence;

import Model.User;
import RoP.Failure;
import RoP.Result;
import RoP.Success;
import Utilities.Hashing;

public class UserRepository {
    public static Result<Boolean> queryLogin(String username, String password) {
        String error;
        var user = getUser(username);
        if (!user.isSuccess()) {
            error = "Invalid username";
        } else if (!Hashing.checkPasswordHash(user.get().getPwHash(), password)) {
            error = "Invalid password";
        } else {
            return new Success<>(true);
        }

        return new Failure<>(error);
    }

    public static Result<String> AddUser(String username, String email, String password1) {
        try {
            var db = DB.connectDb().get();
            db.insert(new User(username, email, Hashing.generatePasswordHash(password1)));
            return new Success<>("OK");
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }





    public static Result<User> getUserById(int userId) {
        var db = DB.connectDb().get();
        var result = db.where("id=?", userId).first(User.class);

        if (result == null) return new Failure<>("No user found for id " + userId);

        return new Success<>(result);
    }



    /*
Convenience method to look up the id for a username.
 */
    public static Result<Integer> getUserId(String username) {
        var user = getUser(username);

        if (!user.isSuccess()) return new Failure<>(user.toString());

        return new Success<>(user.get().id);
    }

    public static Result<User> getUser(String username) {
        var db = DB.connectDb().get();
        var result = db.table("user").where("username=?", username).first(User.class);

        if (result == null) return new Failure<>("No user found for " + username);

        return new Success<>(result);
    }
    public static Result<Long> countUsers() {
        var db = DB.connectDb().get();
        var result = db.sql("select count(*) from user").first(Long.class);
        return new Success<>(result);
    }
}
