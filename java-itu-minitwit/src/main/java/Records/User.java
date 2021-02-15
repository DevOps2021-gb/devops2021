package Records;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "user")
public class User {
    @Id
    @GeneratedValue
    int userId;
    String username;
    String email;
    String pwHash;

    public User(String username, String email, String pwHash) {
        this.username = username;
        this.email = email;
        this.pwHash = pwHash;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPwHash() {
        return pwHash;
    }
}