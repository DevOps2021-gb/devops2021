package Model;

import javax.persistence.Table;

@Table(name = "follower")
public class Follower {
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
