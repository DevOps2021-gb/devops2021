package model;

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

    private String username;
    private String email;
    private String pwHash;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwHash() {
        return pwHash;
    }

    public void setPwHash(String pwHash) {
        this.pwHash = pwHash;
    }
}