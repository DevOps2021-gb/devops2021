package utilities;

import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import services.ILogService;
import services.LogService;

import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Hashing implements IHashing {

    private final ILogService logService;

    public Hashing(ILogService _logService) {
        logService = _logService;
    }

    /*
    Return the gravatar image for the given email address.
    */
    public String getGravatarUrl(String email) {
        String encodedEmail = new String(email.trim().toLowerCase(Locale.ENGLISH).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        String hashHex = generateHashHex(encodedEmail);
        return String.format(Locale.ENGLISH, "http://www.gravatar.com/avatar/%s?d=identicon&s=%d", hashHex, 50);
    }

    public Result<Boolean> checkPasswordHash(String passwordHash, String password) {
        var hash = hash(password);

        if (hash.isSuccess()) {
            return new Success<>(hash.get().equals(passwordHash));
        } else {
            return new Failure<>(hash.getFailureMessage());
        }
    }

    public Result<String> hash(String in) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(in.getBytes(StandardCharsets.UTF_8));
            String hashString = Base64.getEncoder().encodeToString(hashBytes);
            return new Success<>(hashString);
        } catch (NoSuchAlgorithmException e) {
            logService.logError(e, Hashing.class);
            return new Failure<>(e);
        }
    }

    private String generateHashHex(String input) {
        String hashString = hash(input).get();

        StringBuilder sb = new StringBuilder();
        char[] hashChars = hashString.toCharArray();
        for (char hashChar : hashChars) {
            String hexString = Integer.toHexString(hashChar);
            sb.append(hexString);
        }
        return sb.toString();
    }
}
