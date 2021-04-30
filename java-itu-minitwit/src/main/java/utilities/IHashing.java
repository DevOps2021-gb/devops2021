package utilities;

import errorhandling.Result;

public interface IHashing {
    String getGravatarUrl(String email);
    Result<Boolean> checkPasswordHash(String passwordHash, String password);
    Result<String> hash(String in);
}
