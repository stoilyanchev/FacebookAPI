package FacebookReader;

import FBPost.*;

import com.restfb.*;
import com.restfb.Connection;
import com.restfb.types.*;

import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacebookReader {
    private java.sql.Connection connection = null;
    private String token;
    private Boolean parsed;

    public FacebookReader() {
        this.parsed = false;

        try
        {
            this.connection = DriverManager.getConnection("jdbc:sqlite:posts.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate("create table if not exists post (username string, likes integer, comments integer, photo string, createdTime Date, description String)");
        }
        catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void parseUserPosts(String group) {
        if (!this.parsed) {
            try {

                String[] parts = group.split("/");
                String groupId = parts[parts.length - 1];
                FacebookClient facebookClient = new DefaultFacebookClient(this.token, Version.LATEST);
                Connection<Post> postConnection = facebookClient.fetchConnection(groupId + "/feed", Post.class,
                        Parameter.with("fields", "created_time,comments,reactions,from,attachments"),
                        Parameter.with("limit", 1000));
                List<Post> posts = postConnection.getData();
                Map map = new HashMap();

                this.connection.setAutoCommit(false);
                PreparedStatement prep = this.connection.prepareStatement("insert into post values (?, ?, ?, ?, ?, ?);");

                for (Post post : posts) {
                    String author = post.getFrom().getName();
                    Date createdTime = post.getCreatedTime();
                    map.put("createdTime", createdTime);
                    Reactions reactions = post.getReactions();
                    Comments comments = post.getComments();
                    long likesCount = 0;
                    if (reactions != null) {
                        map.put("likes", reactions.getData().size());
                        likesCount = reactions.getData().size();
                    }
                    long commentsCount = 0;
                    if (comments != null) {
                        map.put("comments", comments.getData().size());
                        commentsCount = comments.getData().size();
                    }
                    map.put("username", author);
                    Post.Attachments attachments = post.getAttachments();
                    String photo = "";
                    if (attachments != null) {
                        for (StoryAttachment attachment : attachments.getData()) {
                            if (attachment.getType().equals("photo")) {
                                String imgUrl = attachment.getMedia().getImage().getSrc();
                                String descr = attachment.getDescription();
                                map.put("photo", imgUrl);
                                map.put("description", descr);
                                prep.setString(1, author);
                                prep.setLong(2, likesCount);
                                prep.setLong(3, commentsCount);
                                prep.setString(4, imgUrl);
                                prep.setDate(5, new java.sql.Date(createdTime.getTime()));
                                prep.setString(6, descr);
                                prep.addBatch();
                            }
                        }
                    }
                }
                prep.executeBatch();
                prep.close();
                this.parsed = true;
            } catch(Exception e) {
               System.out.println(e.getMessage());
            }
        }
    }


    public FBPost getUserPost(String user) {
        FBPost post = new FBPost();
        try {
            Statement statement= this.connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from post where username = \"" + user + "\"");
            while (rs.next()) {
                String username = rs.getString("username");
                Long likes = rs.getLong("likes");
                Long comments = rs.getLong("comments");
                String photo = rs.getString("photo");
                Date date = rs.getDate("createdTime");
                String description = rs.getString("description");
                post = new FBPost(username, likes, comments, photo, date, description);
            }
            rs.close();
        } catch(SQLException e)
        {
            System.err.println(e.getMessage());
        }
        return post;
    }
}
