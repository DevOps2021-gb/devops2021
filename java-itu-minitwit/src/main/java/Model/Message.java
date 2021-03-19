package Model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table (name = "message")
public class Message {
    @Id
    @GeneratedValue
    public int id;
    private int authorId;
    private String text;
    private long pubDate;
    private int flagged;

    // must have 0-arg constructor
    public Message() {}
    // must be public or privat with getter and setter
    public Message(int authorId, String text, long pubDate, int flagged) {
        this.authorId = authorId;
        this.text = text;
        this.pubDate = pubDate;
        this.flagged = flagged;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getText() {
        return text;
    }

    public long getPubDate() {
        return pubDate;
    }

    public int getFlagged() {
        return flagged;
    }
}
