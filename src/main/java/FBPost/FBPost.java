package FBPost;

import java.util.Date;

public class FBPost {
    public String username;
    public Long likes;
    public Long comments;
    public String photo;
    public Date createdDate;
    public String description;

    public FBPost() {
        this.username = "";
        this.likes = 0L;
        this.comments = 0L;
        this.photo = "";
        this.createdDate = new Date();
        this.description = "";
    }

    public FBPost(String username, Long likes, Long comments, String photo, Date createdDate, String description) {
        this.username = username;
        this.likes = likes;
        this.comments = comments;
        this.photo = photo;
        this.createdDate = createdDate;
        this.description = description;
    }
}