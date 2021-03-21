package model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "follower")
public class Follower {
    @Id
    @GeneratedValue
    public int id;
    private int whoId;
    private int whomId;

    /**
     * must have 0-arg constructor
     */
    public Follower() {}

    public Follower(final int whoId, final int whomId) {
        this.whoId = whoId;
        this.whomId = whomId;
    }

    //Jinjava needs to have access to getters/setters

    public int getWhoId() {
        return whoId;
    }

    public int getWhomId() {
        return whomId;
    }
}