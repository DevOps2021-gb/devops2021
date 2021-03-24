package model;

public class Tweet {
    private String email;
    private String username;
    private String text;
    private String pubDate;
    private String profilePic;

    public Tweet(final String email, final String username, final String text, final String pubDate, final String profilePic) {
        this.email = email;
        this.username = username;
        this.text = text;
        this.pubDate = pubDate;
        this.profilePic = profilePic;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getProfilePic() {
        return profilePic;
    }
}
