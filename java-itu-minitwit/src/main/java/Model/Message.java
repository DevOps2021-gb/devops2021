package Model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table (name = "message")
public class Message {
    @Id
    @GeneratedValue
    public int id;
    public int authorId;
    public String text;
    public long pubDate;
    public int flagged;

    // must have 0-arg constructor
    public Message() {}
    // must be public or privat with getter and setter
    public Message(int authorId, String text, long pubDate, int flagged) {
        this.authorId = authorId;
        this.text = text;
        this.pubDate = pubDate;
        this.flagged = flagged;
    }

}
