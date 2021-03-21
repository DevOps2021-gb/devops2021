package utilities;

import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import services.LogService;

import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Hashing {

    private Hashing() {}

    private static Result<String> tryGenerateHashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            String hashString = Base64.getEncoder().encodeToString(hashBytes);
            return new Success<>(hashString);
        } catch (NoSuchAlgorithmException e) {
            LogService.logError(e);
            return new Failure<>(e);
        }
    }

    public static Result<String> generatePasswordHash(String password) {
            return tryGenerateHashString(password);
    }

    public static Result<Boolean> checkPasswordHash(String passwordHash, String password) {
        try {
            return new Success<>(generatePasswordHash(password).get().equals(passwordHash));
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    public static String generateHashHex(String input) {
        String hashString = tryGenerateHashString(input).get();

        StringBuilder sb = new StringBuilder();
        char[] hashChars = hashString.toCharArray();
        for (char hashChar : hashChars) {
            String hexString = Integer.toHexString(hashChar);
            sb.append(hexString);
        }
        return sb.toString();
    }

    /*
    Return the gravatar image for the given email address.
    */
    public static String gravatarUrl(String email) {
        String encodedEmail = new String(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        String hashHex = Hashing.generateHashHex(encodedEmail);
        return String.format(Locale.ENGLISH, "http://www.gravatar.com/avatar/%s?d=identicon&s=%d", hashHex, 50);
    }
}
