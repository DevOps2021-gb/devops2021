package Model;

import javax.persistence.*;

@Table (name = "Message")
public class Message {
    @Id
    @GeneratedValue
    public int id;
    @ManyToOne
    @JoinColumn(name = "author")
    private User author;
    public String text;
    public long pubDate;
    public int flagged;

    // must have 0-arg constructor
    public Message() {}
    // must be public or privat with getter and setter
    public Message(User author, String text, long pubDate, int flagged) {
        this.author = author;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
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
