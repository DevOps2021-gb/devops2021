package Records;

import javax.persistence.Table;

@Table(name = "follower")
public class Follower {
    int whoId;
    int whomId;

    public Follower(int whoId, int whomId) {
        this.whoId = whoId;
        this.whomId = whomId;
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
}
