package Records;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table (name = "message")
public class Message {
    @Id
    @GeneratedValue
    int messageId;

    int authorId;
    String text;
    long pubDate;
    int flagged;

    public Message(int authorId, String text, long pubDate, int flagged) {
        this.authorId = authorId;
        this.text = text;
        this.pubDate = pubDate;
        this.flagged = flagged;
    }


    public int getMessageId() {
        return messageId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getText() {
        return text;
    }

    public int getFlagged() {
        return flagged;
    }
}
