
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Hashing {

    private static String generate_hash_string(String input) {
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

    public static String generate_password_hash(String password) {
        return generate_hash_string(password);
    }

    public static boolean check_password_hash(String passwordHash, String password) {
        return generate_password_hash(password).equals(passwordHash);
    }

    public static String generate_hash_hex(String input) {
        String hashString = generate_hash_string(input);

        StringBuffer sb = new StringBuffer();
        char[] hashChars = hashString.toCharArray();
        for(int i = 0; i < hashChars.length; i++) {
            String hexString = Integer.toHexString(hashChars[i]);
            sb.append(hexString);
        }
        return sb.toString();
    }
}
