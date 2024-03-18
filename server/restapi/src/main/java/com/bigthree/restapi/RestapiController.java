package com.bigthree.restapi;

import com.bigthree.restapi.utils.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@RestController
public class RestapiController {

    private Dotenv dotenv;
    private DataBase db;

    public RestapiController() {
        this.dotenv = Dotenv.load();
        try {
            this.db = new DataBase(dotenv.get("DB_URL"), dotenv.get("USER"), dotenv.get("PASS"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public RestapiController(DataBase db){
        this.dotenv = Dotenv.load();
        this.db = db;
    }

    public RestapiController(Dotenv dotenv) {
        this.dotenv = dotenv;
        try {
            this.db = new DataBase(dotenv.get("DB_URL"), dotenv.get("USER"), dotenv.get("PASS"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/verifyLogin")
    public String verifyLogin(@RequestParam String login) {
        boolean data = false;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();
            Connection dbConnection = null;
            Statement statement = null;

            String sql = "SELECT * FROM auth_data WHERE login = \'" + login + "\'";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                data = false;
            } else {
                data = true;
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось проверить доступность логина", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @PostMapping("/regUser")
    public String regUser(@RequestBody String dataJson) {
        int data = 0;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        UserData user = gson.fromJson(dataJson, UserData.class);

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "SELECT * FROM auth_data WHERE login = \'" + user.getLogin() + "\'";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                return gson.toJson(new Message<>("Error", "Логин занят", -1));
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "INSERT INTO auth_data"
                    + "(login, pass) "
                    + "VALUES"
                    + "(\'" + user.getLogin() + "\', "
                    + "\'" + user.getPass() + "\')";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            statement.execute(sql);

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            int loginId = 0;

            sql = "SELECT * FROM auth_data WHERE login = \'" + user.getLogin() + "\'";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            rs = statement.executeQuery(sql);

            while (rs.next()) {
                loginId = rs.getInt("id");
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "INSERT INTO user_info"
                    + "(info, loginid, name, identificator, profilepicture) "
                    + "VALUES"
                    + "(\'\', "
                    + loginId + ", "
                    + "\'" + user.getName() + "\', "
                    + "\'" + "user" + loginId + "\', "
                    + "\'" + "profilePictures/avatar" + ".jpg\')";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            statement.execute(sql);

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "SELECT id FROM user_info WHERE loginid = " + loginId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            rs = statement.executeQuery(sql);

            while (rs.next()) {
                data = rs.getInt("id");
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось зарегистрировать пользователя", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @GetMapping("/loginUser")
    public String loginUser(@RequestParam String login, @RequestParam String pass) {
        int data = 0;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "SELECT * FROM auth_data WHERE login = \'" + login + "\'";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            if (!rs.next()) {
                return gson.toJson(new Message<>("Error", "Неверный логин", -1));
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "SELECT user_info.id FROM user_info"
                    + " JOIN auth_data ON user_info.loginid = auth_data.id"
                    + " WHERE auth_data.login = \'" + login + "\'"
                    + " AND auth_data.pass = \'" + pass + "\'";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            rs = statement.executeQuery(sql);

            if (!rs.next()) {
                return gson.toJson(new Message<>("Error", "Неверный пароль", -1));
            }

            data = rs.getInt("id");

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось выполнить вход", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @GetMapping("/getProfile")
    public String getProfile(@RequestParam int userId) {
        String data = "";

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "SELECT COUNT(*) AS follows_count FROM relationships WHERE followerid = " + userId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            int follows = 0;

            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                follows = rs.getInt("follows_count");
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "SELECT COUNT(*) AS followers_count FROM relationships WHERE subscribeid = " + userId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            int followers = 0;

            rs = statement.executeQuery(sql);

            while (rs.next()) {
                followers = rs.getInt("followers_count");
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "SELECT * FROM user_info WHERE id = " + userId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            rs = statement.executeQuery(sql);

            while (rs.next()) {
                data = gson.toJson(new Profile(rs.getString("identificator"), rs.getString("name"),
                        follows, followers, rs.getString("info"), rs.getString("profilepicture")));
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось загрузить профиль пользователя", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @GetMapping("/getUserPosts")
    public String getUserPosts(@RequestParam int userId,
                               @RequestParam(required = false, defaultValue = "0") int prevPostId) {
        String data = "";

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            Timestamp time = new Timestamp(System.currentTimeMillis());

            if (prevPostId != 0) {

                String sql = "SELECT posttime FROM posts WHERE id = " + prevPostId;

                dbConnection = db.getDBConnection();
                statement = dbConnection.createStatement();

                ResultSet rs = statement.executeQuery(sql);

                while (rs.next()) {
                    time = rs.getTimestamp("posttime");
                }

                if (statement != null) {
                    statement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }
            }

            String sql = "SELECT posts.id, authorid, user_info.profilepicture, user_info.identificator, user_info.name, posttime, posttext,"
                    + " categories.name AS category, postpicture, postlikes"
                    + " FROM posts"
                    + " JOIN categories ON postcategoryid = categories.id"
                    + " JOIN user_info ON authorid = user_info.id"
                    + " WHERE authorid = " + userId + " AND posttime < \'" + time + "\'"
                    + " ORDER BY posttime DESC LIMIT 3";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            List<Post> postList = new ArrayList<>();

            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                postList.add(new Post(rs.getInt("id"), rs.getInt("authorid"),
                        rs.getString("profilepicture"), rs.getString("identificator"),
                        rs.getString("name"), rs.getTimestamp("posttime"),
                        rs.getString("posttext"), rs.getString("category"),
                        rs.getString("postpicture"), rs.getInt("postlikes"),
                        isLiked(userId, rs.getInt("id"))));
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            data = gson.toJson(postList);

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось загрузить посты пользователя", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @GetMapping("/getIsSubscribe")
    public String getIsSubscribe(@RequestParam int followerId, @RequestParam int userId) {
        boolean data = false;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "SELECT * FROM relationships WHERE followerid = " + followerId
                    + " AND subscribeid = " + userId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                data = true;
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось проверить подписку на пользователя", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @PostMapping("/followUser")
    public String followUser(@RequestBody String dataJson) {
        int data = 0;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        RelationshipsData relations = gson.fromJson(dataJson, RelationshipsData.class);

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "INSERT INTO relationships"
                    + "(followerid, subscribeid)"
                    + " VALUES"
                    + "(" + relations.getFollowerId() + ", " + relations.getUserId() + ")";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            statement.execute(sql);

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "SELECT COUNT(*) AS follows_count FROM relationships WHERE subscribeid = " + relations.getUserId();

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                data = rs.getInt("follows_count");
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось подписаться на пользователя", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @DeleteMapping("/unfollowUser")
    public String unfollowUser(@RequestBody String dataJson) {
        int data = 0;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        RelationshipsData relations = gson.fromJson(dataJson, RelationshipsData.class);

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "DELETE FROM relationships"
                    + " WHERE followerid = " + relations.getFollowerId()
                    + " AND subscribeid = " + relations.getUserId();

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            statement.execute(sql);

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "SELECT COUNT(*) AS follows_count FROM relationships WHERE subscribeid = " + relations.getUserId();;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                data = rs.getInt("follows_count");
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось отписаться от пользователя", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @PutMapping("/changeProfile")
    public String changeProfile(@RequestBody String dataJson) {
        String data = "";

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        ProfileData profile = gson.fromJson(dataJson, ProfileData.class);

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "SELECT identificator FROM user_info WHERE id = " + profile.getId();

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                String identificator = rs.getString("identificator");

                if (profile.getIdentificator().startsWith("user")
                        && (!identificator.equals(profile.getIdentificator()))) {
                    return gson.toJson(new Message<>("Error", "Идентификатор не может начинаться с 'user'", -1));
                }
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "SELECT id FROM user_info WHERE identificator = \'" + profile.getIdentificator() + "\'"
                    + " AND id != " + profile.getId();

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            rs = statement.executeQuery(sql);

            if (rs.next()) {
                return gson.toJson(new Message<>("Error", "Данный идентификатор уже занят.", -1));
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            if (profile.getImagePath().contains("profilePictures")) {
                data = profile.getImagePath();
            } else {
                data = loadImage(profile.getImagePath(), "profilePictures");

                sql = "SELECT profilepicture FROM user_info WHERE id = " + profile.getId();

                dbConnection = db.getDBConnection();
                statement = dbConnection.createStatement();

                String picPath = "";

                rs = statement.executeQuery(sql);

                if (rs.next()) {
                    picPath = rs.getString("profilepicture");
                }

                if (statement != null) {
                    statement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }

                if (!picPath.contains("avatar")) {
                    Path path = Paths.get("");

                    String filepath = path.toAbsolutePath().toString();

                    char delimitter;

                    if (filepath.charAt(0) == '/') {
                        delimitter = '/';
                    } else {
                        delimitter = '\\';
                    }

                    filepath = filepath.substring(0, filepath.indexOf(delimitter + "server"));

                    deleteFile(filepath + delimitter + "client" + delimitter + "public" + delimitter + picPath);
                }
            }

            sql = "UPDATE user_info SET identificator = \'" + profile.getIdentificator() + "\'"
                    + ", name = \'" + profile.getName() + "\', " + " info = \'" + profile.getInfo() + "\'"
                    + ", profilepicture = \'" + data + "\' WHERE id = " + profile.getId();

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            statement.execute(sql);

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось изменить информацию профиля", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();

        } catch (Throwable e) {
            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось загрузить изображение", -1));

        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @GetMapping("/verifyIdentificator")
    public String verifyIdentificator(@RequestParam String identificator,
                                      @RequestParam(required = false, defaultValue = "0") int userId) {
        boolean data = false;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "SELECT * FROM user_info WHERE identificator = \'" + identificator + "\'";

            if (userId != 0) {
                sql += " AND id != " + userId;
            }

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                data = false;
            } else {
                data = true;
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось проверить доступность идентификатора", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @PostMapping("/createPost")
    public String createPost(@RequestBody String dataJson) {
        String data = "";

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        PostData post = gson.fromJson(dataJson, PostData.class);

        String imagePath = "";

        try {
            if (!post.getImage().equals("")) {
                imagePath = loadImage(post.getImage(), "postPictures");
            }

            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "INSERT INTO posts(authorid, posttime, postcategoryid, postpicture, posttext, postlikes)"
                    + " VALUES(" + post.getAuthorId() + ", \'" + new Timestamp(System.currentTimeMillis()) + "\'"
                    + ", " + post.getCategoryId() + ", \'" + imagePath + "\', \'" + post.getText() + "\'"
                    + ", " + 0 + ")";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            statement.execute(sql);

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "SELECT id, posttime FROM posts WHERE postpicture = \'" + imagePath + "\'";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            int postId = 0;
            Timestamp postTime = new Timestamp(0);

            while (rs.next()) {
                postId = rs.getInt("id");
                postTime = rs.getTimestamp("posttime");
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            JsonObject json = new JsonObject();

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss aaa", Locale.ENGLISH);

            json.addProperty("postId", postId);
            json.addProperty("postTime", dateFormat.format(postTime));
            json.addProperty("imagePath", imagePath);

            data = json.toString();

        }  catch (SQLException e) {
            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось создать пост", -1));

        }  catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();

        }  catch (Throwable e) {
            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось загрузить изображение", -1));

        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @PutMapping("/incLikesOnPost")
    public String incLikesOnPost(@RequestParam int userId, @RequestParam int postId) {
        int data = 0;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            boolean isLiked = isLiked(userId, postId);

            String sql = "";
            Connection dbConnection = null;
            Statement statement = null;

            if (!isLiked) {
                sql = "UPDATE posts SET postlikes = postlikes + 1"
                        + " WHERE id = " + postId;

                dbConnection = db.getDBConnection();
                statement = dbConnection.createStatement();

                statement.execute(sql);

                if (statement != null) {
                    statement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }

                sql = "INSERT INTO likes (userid, postid) VALUES (" + userId +", "+ postId + ")";

                dbConnection = db.getDBConnection();
                statement = dbConnection.createStatement();
                statement.execute(sql);

                if (statement != null) {
                    statement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }
            }

            sql = "SELECT postlikes FROM posts WHERE id = " + postId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                data = rs.getInt("postlikes");
            }

            System.out.println(data);

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось поставить лайк посту", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @PutMapping("/decLikesOnPost")
    public String decLikesOnPost(@RequestParam int userId, @RequestParam int postId) {
        int data = 0;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            boolean isLiked = isLiked(userId, postId);

            String sql = "";
            Connection dbConnection = null;
            Statement statement = null;

            if (isLiked) {
                sql = "UPDATE posts SET postlikes = postlikes - 1"
                        + " WHERE id = " + postId;

                dbConnection = db.getDBConnection();
                statement = dbConnection.createStatement();

                statement.execute(sql);

                if (statement != null) {
                    statement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }

                sql = "DELETE FROM likes WHERE userId = " + userId + "AND postId = " + postId;

                dbConnection = db.getDBConnection();
                statement = dbConnection.createStatement();

                statement.execute(sql);

                if (statement != null) {
                    statement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }
            }

            sql = "SELECT postlikes FROM posts WHERE id = " + postId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                data = rs.getInt("postlikes");
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось убрать лайк с поста", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @GetMapping("/getPost")
    public String getPost(@RequestParam int userId, @RequestParam int postId) {
        String data = "";

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "SELECT posts.id, authorid, user_info.profilepicture, user_info.identificator, user_info.name, posttime, posttext,"
                    + " categories.name AS category, postpicture, postlikes"
                    + " FROM posts"
                    + " JOIN categories ON postcategoryid = categories.id"
                    + " JOIN user_info ON authorid = user_info.id"
                    + " WHERE posts.id = " + postId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            if (!rs.next()) {
                return gson.toJson(new Message<>("Error", "Пост был удален", -1));
            } else {
                data = gson.toJson(new Post(rs.getInt("id"), rs.getInt("authorid"),
                        rs.getString("profilepicture"), rs.getString("identificator"),
                        rs.getString("name"), rs.getTimestamp("posttime"),
                        rs.getString("posttext"), rs.getString("category"),
                        rs.getString("postpicture"), rs.getInt("postlikes"),
                        isLiked(userId, rs.getInt("id"))));
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось загрузить пост", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @DeleteMapping("/deletePost")
    public String deletePost(@RequestParam int postId) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "SELECT postpicture FROM posts WHERE id = " + postId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            String picPath = "";

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                picPath = rs.getString("postpicture");
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            Path path = Paths.get("");

            String filepath = path.toAbsolutePath().toString();

            char delimitter;

            if (filepath.charAt(0) == '/') {
                delimitter = '/';
            } else {
                delimitter = '\\';
            }

            filepath = filepath.substring(0, filepath.indexOf(delimitter + "server"));

            deleteFile(filepath + delimitter + "client" + delimitter + "public" + delimitter + picPath);

            sql = "DELETE FROM posts WHERE id = " + postId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();
            statement.execute(sql);

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "DELETE FROM likes WHERE postid = " + postId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();
            statement.execute(sql);

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            sql = "DELETE FROM commentaries WHERE postid = " + postId;

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();
            statement.execute(sql);

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось удалить пост", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", 1));
    }

    @GetMapping("/getIdByIdentificator")
    public String getIdByIdentificator(@RequestParam String identificator) {
        int data = -1;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            String sql = "SELECT id FROM user_info WHERE identificator = \'" + identificator + "\'";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                data = rs.getInt("id");
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось проверить идентификатор", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @GetMapping("/getPosts")
    public String getPosts(@RequestParam int userId,
                           @RequestParam(required = false, defaultValue = "0") int prevPostId,
                           @RequestParam(required = false, defaultValue = "0") int categoryId) {
        String data = "";

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            Timestamp time = new Timestamp(System.currentTimeMillis());

            if (prevPostId != 0) {
                String sql = "SELECT posttime FROM posts WHERE id = " + prevPostId;

                dbConnection = db.getDBConnection();
                statement = dbConnection.createStatement();

                ResultSet rs = statement.executeQuery(sql);

                while (rs.next()) {
                    time = rs.getTimestamp("posttime");
                }

                if (statement != null) {
                    statement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }
            }

            String sql = "";

            if (categoryId == 0) {

                sql = "SELECT posts.id, authorid, user_info.profilepicture, user_info.identificator,"
                        + " user_info.name, posttime, categories.name AS category, postpicture, posttext, postlikes"
                        + " FROM posts JOIN user_info ON authorid = user_info.id"
                        + " JOIN categories ON postcategoryid = categories.id"
                        + " WHERE  posttime < \'" + time + "\' AND authorid != " + userId
                        + " ORDER BY posttime DESC LIMIT 3";
            } else {
                sql = "SELECT posts.id, authorid, user_info.profilepicture, user_info.identificator,"
                        + " user_info.name, posttime, categories.name AS category, postpicture, posttext, postlikes"
                        + " FROM posts JOIN user_info ON authorid = user_info.id"
                        + " JOIN categories ON postcategoryid = categories.id"
                        + " WHERE  posttime < \'" + time + "\' AND authorid != " + userId + " AND categories.id = " + categoryId
                        + " ORDER BY posttime DESC LIMIT 3";
            }

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            List<Post> postList = new ArrayList<>();

            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                postList.add(new Post(rs.getInt("id"), rs.getInt("authorid"),
                        rs.getString("profilepicture"), rs.getString("identificator"),
                        rs.getString("name"), rs.getTimestamp("posttime"),
                        rs.getString("posttext"), rs.getString("category"),
                        rs.getString("postpicture"), rs.getInt("postlikes"),
                        isLiked(userId, rs.getInt("id"))));
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            data = gson.toJson(postList);

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось загрузить посты новостной ленты", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    @GetMapping("/getSubPosts")
    public String getSubPosts(@RequestParam int userId,
                              @RequestParam(required = false, defaultValue = "0") int prevPostId,
                              @RequestParam(required = false, defaultValue = "0") int categoryId) {
        String data = "";

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try {
            db.initialSetUp();

            Connection dbConnection = null;
            Statement statement = null;

            Timestamp time = new Timestamp(System.currentTimeMillis());

            if (prevPostId != 0) {
                String sql = "SELECT posttime FROM posts WHERE id = " + prevPostId;

                dbConnection = db.getDBConnection();
                statement = dbConnection.createStatement();

                ResultSet rs = statement.executeQuery(sql);

                while (rs.next()) {
                    time = rs.getTimestamp("posttime");
                }

                if (statement != null) {
                    statement.close();
                }
                if (dbConnection != null) {
                    dbConnection.close();
                }
            }

            String sql = "";

            if (categoryId == 0) {

                sql = "SELECT posts.id, authorid, user_info.profilepicture, user_info.identificator,"
                        + " user_info.name, posttime, categories.name AS category, postpicture, posttext, postlikes"
                        + " FROM posts JOIN user_info ON authorid = user_info.id"
                        + " JOIN categories ON postcategoryid = categories.id"
                        + " JOIN relationships ON relationships.followerid = \'"+userId+"\'"
                        + " WHERE  posttime < \'" + time + "\' AND authorid = relationships.subscribeid"
                        + " ORDER BY posttime DESC LIMIT 3";
            } else {
                sql = "SELECT posts.id, authorid, user_info.profilepicture, user_info.identificator,"
                        + " user_info.name, posttime, categories.name AS category, postpicture, posttext, postlikes"
                        + " FROM posts JOIN user_info ON authorid = user_info.id"
                        + " JOIN categories ON postcategoryid = categories.id"
                        + " JOIN relationships ON relationships.followerid = \'"+userId+"\'"
                        + " WHERE  posttime < \'" + time + "\' AND authorid = relationships.subscribeid AND categories.id = " + categoryId
                        + " ORDER BY posttime DESC LIMIT 3";
            }

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            List<Post> postList = new ArrayList<>();

            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                postList.add(new Post(rs.getInt("id"), rs.getInt("authorid"),
                        rs.getString("profilepicture"), rs.getString("identificator"),
                        rs.getString("name"), rs.getTimestamp("posttime"),
                        rs.getString("posttext"), rs.getString("category"),
                        rs.getString("postpicture"), rs.getInt("postlikes"),
                        isLiked(userId, rs.getInt("id"))));
            }

            if (statement != null) {
                statement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }

            data = gson.toJson(postList);

        } catch (SQLException e) {

            e.printStackTrace();
            return gson.toJson(new Message<>("Error", "Не удалось загрузить посты ленты подписок", -1));

        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found");
            e.printStackTrace();
        }

        return gson.toJson(new Message<>("Success", "", data));
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);

        System.out.println(filePath);

        if (file.delete()) {
            System.out.println("File was deleted");
        } else {
            System.out.println("Wrong filepath!");
        }
    }

    private String loadImage(String url, String foldername) throws IOException {
        Path path = Paths.get("");

        String filePath = path.toAbsolutePath().toString();
        
        char delimitter;
        
        if(filePath.charAt(0)=='/'){
            delimitter = '/';
        } else {
            delimitter = '\\';
        }
        
        filePath = filePath.substring(0, filePath.indexOf(delimitter + "server"));

        File folder = new File(filePath + delimitter + "client" + delimitter + "public" + delimitter + foldername);

        if (!folder.exists()) {
            folder.mkdir();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        String filename = sdf.format(System.currentTimeMillis()) + ".jpg";
        File imgFile = new File(filePath + delimitter + "client" + delimitter + "public" + delimitter + foldername, filename);

        byte[] imgData = Base64.getDecoder().decode(url);

        OutputStream stream = new FileOutputStream(imgFile);

        if (!imgFile.exists()) {
            imgFile.createNewFile();
        }

        stream.write(imgData);
        stream.close();

        return foldername + '/' + filename;
    }

    private boolean isLiked (int userId, int postId) throws SQLException, ClassNotFoundException {
        db.initialSetUp();

        Connection dbConnection = null;
        Statement statement = null;

        String sql = "SELECT * FROM likes WHERE userid = " + userId + " AND postid =" + postId;

        dbConnection = db.getDBConnection();
        statement = dbConnection.createStatement();

        boolean isLiked = false;

        ResultSet rs = statement.executeQuery(sql);

        if (rs.next()) {
            isLiked = true;
        }

        if (statement != null) {
            statement.close();
        }
        if (dbConnection != null) {
            dbConnection.close();
        }

        return isLiked;
    }
}
