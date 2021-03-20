package model;

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

    /**
     * must have 0-arg constructor
     */
    public Message() {}

    public Message(final int authorId, final String text, final long pubDate, final int flagged) {
        this.authorId = authorId;
        this.text = text;
        this.pubDate = pubDate;
        this.flagged = flagged;
    }

    //Jinjava needs to have access to getters/setters

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
