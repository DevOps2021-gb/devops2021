package model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "user")
public class User {
    @Id
    @GeneratedValue
    public int id;

    private String username;
    private String email;
    private String pwHash;

    /**
     * must have 0-arg constructor
     */
    public User() {}

    public User(final String username, final String email, final String pwHash) {
        this.username = username;
        this.email = email;
        this.pwHash = pwHash;
    }

    //Jinjava needs to have access to getters/setters

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPwHash() {
        return pwHash;
    }

    public void setPwHash(final String pwHash) {
        this.pwHash = pwHash;
    }
}