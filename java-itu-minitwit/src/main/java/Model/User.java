package Model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "user")
public class User {

    // must have 0-arg constructor
    public User() {}

    public User(String username, String email, String pwHash) {
        this.username = username;
        this.email = email;
        this.pwHash = pwHash;
    }

    // must be public or privat with getter and setter
    @Id
    @GeneratedValue
    public int id;

    public String username;
    public String email;
    public String pwHash;

}