package Model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "Follower")
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWhoId() {
        return whoId;
    }

    public void setWhoId(int whoId) {
        this.whoId = whoId;
    }

    public int getWhomId() {
        return whomId;
    }

    public void setWhomId(int whomId) {
        this.whomId = whomId;
    }

    @Override
    public String toString() {
        return "Follower{" +
                "id=" + id +
                ", whoId=" + whoId +
                ", whomId=" + whomId +
                '}';
    }
}
