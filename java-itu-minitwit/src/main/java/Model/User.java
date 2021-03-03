package Model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Table(name = "User")
public class User {
    // must be public or privat with getter and setter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;

    public String username;
    public String email;
    public String pwHash;
    @OneToMany(fetch = FetchType.EAGER, mappedBy="Follower")
    private Set<Follower> follower_who;
    @OneToMany(fetch = FetchType.EAGER, mappedBy="Follower")
    private Set<Follower> follower_whom;
    @OneToMany(fetch = FetchType.EAGER, mappedBy="Message")
    private Set<Follower> messages;

    // must have 0-arg constructor
    public User() {}

    public User(String username, String email, String pwHash) {
        this.username = username;
        this.email = email;
        this.pwHash = pwHash;
        follower_who = new HashSet<>();
        follower_whom = new HashSet<>();
    }

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

    public Set<Follower> getFollower_who() {
        return follower_who;
    }

    public void setFollower_who(Set<Follower> follower_who) {
        this.follower_who = follower_who;
    }

    public Set<Follower> getFollower_whom() {
        return follower_whom;
    }

    public void setFollower_whom(Set<Follower> follower_whom) {
        this.follower_whom = follower_whom;
    }

    public Set<Follower> getMessages() {
        return messages;
    }

    public void setMessages(Set<Follower> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", pwHash='" + pwHash + '\'' +
                ", follower_who=" + follower_who +
                ", follower_whom=" + follower_whom +
                '}';
    }
}