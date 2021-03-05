package Model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "follower")
public class Follower {
    @Id
    @GeneratedValue
    public int id;
    public int whoId;
    public int whomId;

    // must have 0-arg constructor
    public Follower() {

    }

    public Follower(int whoId, int whomId) {
        this.whoId = whoId;
        this.whomId = whomId;
    }

}