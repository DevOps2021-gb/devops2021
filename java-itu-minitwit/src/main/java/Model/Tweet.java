package Model;

public class Tweet {
    public String email;
    public String username;
    public String text;
    public String pubDate;
    public String profilePic;

    public Tweet(String email, String username, String text, String pubDate, String profilePic) {
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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
