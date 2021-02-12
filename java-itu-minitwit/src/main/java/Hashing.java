import org.apache.commons.lang3.NotImplementedException;

public class Hashing {
    public static String generate_password_hash(String password) {
        return password;    //todo remove
        //throw new NotImplementedException("Not implemented");
    }

    public static boolean check_password_hash(String password1, String password2) {
        return password1.equals(password2);    //todo remove
        //throw new NotImplementedException("Not implemented");
    }
}
