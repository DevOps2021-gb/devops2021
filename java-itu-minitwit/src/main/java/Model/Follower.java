package Model;

import javax.persistence.*;

@Table(name = "Follower")
public class Follower {
    @Id
    @GeneratedValue
    public int id;
    @ManyToOne
    @JoinColumn(name = "who")
    private User who;
    @ManyToOne
    @JoinColumn(name = "whom")
    private User whom;

    // must have 0-arg constructor
    public Follower() {

    }

    public Follower(User who, User whom) {
        this.who = who;
        this.whom = whom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getWho() {
        return who;
    }

    public void setWho(User who) {
        this.who = who;
    }

    public User getWhom() {
        return whom;
    }

    public void setWhom(User whom) {
        this.whom = whom;
    }

    @Override
    public String toString() {
        return "Follower{" +
                "id=" + id +
                ", who=" + who +
                ", whom=" + whom +
                '}';
    }
}
