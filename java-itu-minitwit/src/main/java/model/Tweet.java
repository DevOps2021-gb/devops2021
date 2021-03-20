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

    //Jinjava needs to have access to getters/setters

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
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

    public void setPubDate(final String pubDate) {
        this.pubDate = pubDate;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(final String profilePic) {
        this.profilePic = profilePic;
    }
}
