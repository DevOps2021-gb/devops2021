package Model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

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


    public int getId() {
        return id;
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

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPwHash(String pwHash) {
        this.pwHash = pwHash;
    }
}