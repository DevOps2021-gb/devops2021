
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Hashing {

    private Hashing() {}

    private static String generateHashString(String input) {
        String hashString = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            hashString = Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashString;
    }

    public static String generatePasswordHash(String password) {
        return generateHashString(password);
    }

    public static boolean checkPasswordHash(String passwordHash, String password) {
        return generatePasswordHash(password).equals(passwordHash);
    }

    public static String generateHashHex(String input) {
        String hashString = generateHashString(input);

        StringBuilder sb = new StringBuilder();
        char[] hashChars = hashString.toCharArray();
        for (char hashChar : hashChars) {
            String hexString = Integer.toHexString(hashChar);
            sb.append(hexString);
        }
        return sb.toString();
    }
}
