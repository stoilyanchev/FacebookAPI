import FacebookReader.*;
import FBPost.*;

import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.*;

import static spark.Spark.*;


public class Main {

    public static void main(String[] argv) {
        Map map = new HashMap();
        staticFileLocation("/templates");
        FacebookReader fb = new FacebookReader();

        get("/", (request, response) -> {
            return new ModelAndView(null, "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/search_invitation", (request, response) -> {
            return new ModelAndView(null, "search_invitation.hbs");
        }, new HandlebarsTemplateEngine());

        post("/search_invitation", (request, response) -> {
            String[] folder = request.queryParamsValues("folder");
            List<String> invitations = new ArrayList<String>();
            for (String file : folder ) {
                String re = "^\\d+_invite.(png|jpg)";
                if(file.matches(re)) {
                    invitations.add(file);
                }
            }

            map.put("invitations", invitations);
            return new ModelAndView(map, "search_invitation.hbs");
        }, new HandlebarsTemplateEngine());

        post("/facebookInvitations", (request, response) -> {
            String username = request.queryParams("username");
            String group = request.queryParams("group");
            String token = request.queryParams("token");
            fb.setToken(token);
            try {
                fb.parseUserPosts(group);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            FBPost post = fb.getUserPost(username);
            map.put("username", post.username);
            map.put("comments", post.comments);
            map.put("photo", post.photo);
            map.put("likes", post.likes);
            map.put("createdTime", post.createdDate);
            map.put("description", post.description);
            return new ModelAndView(map, "facebookInvitations.hbs");
        }, new HandlebarsTemplateEngine());

        get("/facebookInvitations", (request, response) -> {
            return new ModelAndView(null, "facebookInvitations.hbs");
        }, new HandlebarsTemplateEngine());
    }
}
