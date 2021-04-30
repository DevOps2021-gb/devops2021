package repository;

import errorhandling.Result;
import model.User;

public interface IUserRepository {
    Result<Boolean> queryLogin(String username, String password);
    Result<String> addUser(String username, String email, String password1);
    Result<User> getUserById(int userId);
    Result<Integer> getUserId(String username);
    Result<User> getUser(String username);
    Result<Long> countUsers();
}
