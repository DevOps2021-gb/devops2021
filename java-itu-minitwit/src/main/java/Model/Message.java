package Model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table (name = "Message")
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getPubDate() {
        return pubDate;
    }

    public void setPubDate(long pubDate) {
        this.pubDate = pubDate;
    }

    public int getFlagged() {
        return flagged;
    }

    public void setFlagged(int flagged) {
        this.flagged = flagged;
    }
}
