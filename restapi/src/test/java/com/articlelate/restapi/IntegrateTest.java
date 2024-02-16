package com.articlelate.restapi;

import com.articlelate.restapi.utils.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class IntegrateTest {
    private Dotenv dotenv;
    private DataBase db;

    public IntegrateTest() {
        this.dotenv = Dotenv.load();
        try {
            this.db = new DataBase(dotenv.get("DB_URL"), dotenv.get("USER"), dotenv.get("PASS"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void initEach() {
        JsonObject json1 = new JsonObject();
        JsonObject json2 = new JsonObject();

        json1.addProperty("name", "testUser1");
        json1.addProperty("login", "testUser1");
        json1.addProperty("pass", "testPassword");

        rest.regUser(json1.toString());

        json2.addProperty("name", "testUser2");
        json2.addProperty("login", "testUser2");
        json2.addProperty("pass", "testPassword");

        rest.regUser(json2.toString());
    }

    @AfterEach
    public void dropEach() throws ClassNotFoundException {
        try {
            Connection dbConnection = null;
            Statement statement = null;

            String sql = "DROP TABLE auth_data, categories, commentaries, likes, notifications,\n" +
                    "posts, relationships, user_info CASCADE";

            dbConnection = db.getDBConnection();
            statement = dbConnection.createStatement();

            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    RestapiController rest = new RestapiController();


    @Test
    void regUserSuccess() {
        JsonObject json = new JsonObject();

        json.addProperty("name", "testUser3");
        json.addProperty("login", "testUser3");
        json.addProperty("pass", "testPassword");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<String> message = gson.fromJson(rest.regUser(json.toString()), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(3.0, message.getData());

        Message<Boolean> message2 = gson.fromJson(rest.verifyLogin("testUser3"), Message.class);

        assertEquals("Success", message2.getState());
        assertFalse(message2.getData());
    }

    @DisplayName("Unsuccessful registration")
    @Test
    void regUserUnsuccessful() throws SQLException, ClassNotFoundException {
        JsonObject json = new JsonObject();

        json.addProperty("name", "testUser1");
        json.addProperty("login", "testUser1");
        json.addProperty("pass", "testPassword");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<String> message = gson.fromJson(rest.regUser(json.toString()), Message.class);

        int data = 0;

        Connection dbConnection = null;
        Statement statement = null;

        String sql = "SELECT COUNT(*) AS count FROM auth_data";

        dbConnection = db.getDBConnection();
        statement = dbConnection.createStatement();

        ResultSet rs = statement.executeQuery(sql);

        if (rs.next()) {
            data = rs.getInt("count");
        }

        assertEquals("Error", message.getState());
        assertEquals("Логин занят", message.getMessage());
        assertEquals(-1.0, message.getData());
        assertEquals(2, data);
    }

    @DisplayName("Login success")
    @Test
    void loginSuccess(){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<Boolean> message = gson.fromJson(rest.loginUser("testUser1", "testPassword"), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(1.0, message.getData());
    }

    @DisplayName("Login unsuccessful incorrect login")
    @Test
    void loginUnsuccessIncorrectLogin(){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<Boolean> message = gson.fromJson(rest.loginUser("wrongUser", "password"), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Неверный логин", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("Success follow")
    @Test
    void followUser() {
        JsonObject json = new JsonObject();

        json.addProperty("followerId", 2);
        json.addProperty("userId", 1);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<Double> message1 = gson.fromJson(rest.followUser(json.toString()), Message.class);

        assertEquals("Success", message1.getState());
        assertEquals(1.0, message1.getData());

        Message<Boolean> message2 = gson.fromJson(rest.getIsSubscribe(2, 1), Message.class);

        assertEquals("Success", message2.getState());
        assertTrue(message2.getData());
    }

    @DisplayName("Post create success test")
    @Test
    public void postCreateTest() throws JSONException {
        JsonObject json = new JsonObject();

        json.addProperty("authorId", 1);
        json.addProperty("categoryId", 1);
        json.addProperty("text", "testText");
        json.addProperty("image", "iVBORw0KGgoAAAANSUhEUgAAAPQAAADPCAMAAAD1TAyiAAAA+VBMVEX39/f6AAAncyz6ooJieBkFAwMAAwP/AAD+/P7/pIYjcy1leBfsnnlUdQkAaAze5d6fh0dcdxP6p4b3//9JdiMbbyH6UEF9oX/6knX6blgAZgAUbRuiuqPSAQFddxzQlWiAfzE+dSZSdiBOhVH4z88eAwPD0cP5Z2cydCr38vKTAgLIzbpbcgD42Nj5XFz6HR36Pz/wAQE/fkOyjFSwuZtoAwOqAgL5iYmLAgL4qKj5e3v6QzYtAwOQgz3R29J8AwMAWwBGbwD34ODArJBwfTJdAwP6UFDhAQH4srL6LiX6clsUAwM7AwNPaQBbeS3o7OgvbQpqlmxyj1iAOB7VAAADtUlEQVR4nO3daVPaUBSAYTbFAAlCRCruihsVLW5dsdrNWlu1/f8/pn7jnMzkGjAhJLzvV2ZMHm+YyWG7mQwRERERUdDmdHGfTlRVX6t2VSlVz539ORRdL4uck9SiDwuirVJOtA06PYEGDTpdgQYNOl1NK7o2KL3oL13Z1w1R/9u+bLsqu4z7xF+Qe5SVLdUt0XqtLKo5sqsEr7u750HnB1nrtnyGr6mLvQU6WYEGDRo06CQGGjToaULX0oJ2s4YUOm+p8racucot2VU1bpY59zgwWldRF3tZrbsDevICDRo0aNCgQU9uoFOMdlUaXVQt1VUJRh9kj2XafD6zM2j2ItsblF2sJxntX/F8VnZRlA+mGD0zCDRo0KBBT06gQYMGnUS0GqsyJvT31Nx7d38cyQzo7M2qrKceSxa6bWJ611qmH0ov2hBo0KBBgwYNGjRo0M+k5qquiVI03G0nC91+K/tgMnfeyW6Si3bfm1ZMo2fUBL1qWOsUoXfUayWgQYMGDRo0aNCgQQ8HlU3JvXf7/U+Raa7yoDuyZE1ZbnCmV53ceXp0dPBAgwYNGjRo0KBBgx4KHfz+etLRbVl3z3C2PfUxQNP06On2o2wpfrR7G/TUi6tqZJ4Njv41ad/Ac98MgZYvjuwER0/cdy1BgwYNGjRo0KBBJwotuwsFbZnQrRDRB7KMRvdUHvTdJ9HvkdEV0dNKr4kKJ7JceGjTbyAWP6vV7ChYcdR52vNrkte2rCRrRbVV3DNo+bztjPxKgQkdx++GggYNGjRo0KBBgwY9HrS+jTej/QeO0dGW/35ZhajQVbUVsm3V/VscdZLK+/9N69QW28HVttRclctFhVYb79lW3rf64qir6f8389ap2ppgywMFDRo0aNCgQYMGDTpNaLUPsGfDqijQ+hCnaq7yoKPalbjaUls+b6gq6mxfjZo2z6tD3KvD7yvz9oPagDo0c6bqyMOUCupi1+hwsvrqgl4wXNCR7TTuQZfHjC6DBg0aNGjQoEGDnk701bJMbQNsV6wI6hfkMTS6Ic/FOYkK/bCretyU9ecj6F4dQs1Vjb/6bCIye9+1bKmLvRZF+/4XdKMZ0Schzf8BjV4rhF950/9J/IQeExQ0aNCgQYMGDRr0RKH1zGXLD9apN71e0OOyb0486BXVmerfQig1V/zbjQPtmbl0zUYphJxL0zHiMJt6Qvs/G4PnXMYNGSbQoEGDBg06IYEGnWq00wihVrLQK81QmoDN64bINB0NUdwMIiIiIhpL/wGJY9+ssJDbRQAAAABJRU5ErkJggg==");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<String> message = gson.fromJson(rest.createPost(json.toString()), Message.class);

        org.json.JSONObject obj = new JSONObject(message.getData());
        String way = obj.getString("imagePath");
        Integer postId = obj.getInt("postId");
        String postTime = obj.getString("postTime");

        Path path = Paths.get("");

        String filePath = path.toAbsolutePath().toString();

        char delimitter;

        if(filePath.charAt(0)=='/'){
            delimitter = '/';
        } else {
            delimitter = '\\';
        }

        filePath = filePath.substring(0, filePath.indexOf(delimitter+"server"));

        File img = new File(filePath + delimitter+"client"+delimitter+"public"+delimitter
                + way.substring(0, way.indexOf("/"))
                + delimitter + way.substring(way.indexOf("/")));

        assertEquals("Success", message.getState());
        assertEquals(1, postId);
        assertTrue(img.exists());

        message = gson.fromJson(rest.getPost(1, postId), Message.class);
        Post post = gson.fromJson(message.getData(), Post.class);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss aaa", Locale.ENGLISH);

        assertEquals(message.getState(), "Success");
        assertEquals(post.getId(), postId);
        assertEquals(post.getAuthorImage(), "profilePictures/avatar.jpg");
        assertEquals(post.getIdentificator(), "user1");
        assertEquals(post.getName(), "testUser1");
        assertEquals(dateFormat.format(post.getTime()), postTime);
        assertEquals(post.getText(), "testText");
        assertEquals(post.getCategory(), "IT");
        assertEquals(post.getImage(), way);
        assertEquals(post.getLikesCount(), 0);
        assertFalse(post.isLiked());

        img.delete();
    }

    @DisplayName("Post create error test")
    @Test
    public void postCreateErrorTest() {
        JsonObject json = new JsonObject();

        json.addProperty("authorId", 1);
        json.addProperty("categoryId", 2);
        json.addProperty("text", "text");
        json.addProperty("image", "iVBORw0KGgoAAAANSUhEUgAAAPQAAADPCAMAAAD1TAyiAAAA+VBMVEX39/f6AAAncyz6ooJiFAwMAAwP/AAD+/P7/pIYjcy1leBfsnnlUdQkAaAze5d6fh0dcdxP6p4b3//9JdiMbbyH6UEF9oX/6knX6blgAZgAUbRuiuqPSAQFddxzQlWiAfzE+dSZSdiBOhVH4z88eAwPD0cP5Z2cydCr38vKTAgLIzbpbcgD42Nj5XFz6HR36Pz/wAQE/fkOyjFSwuZtoAwOqAgL5iYmLAgL4qKj5e3v6QzYtAwOQgz3R29J8AwMAWwBGbwD34ODArJBwfTJdAwP6UFDhAQH4srL6LiX6clsUAwM7AwNPaQBbeS3o7OgvbQpqlmxyj1iAOB7VAAADtUlEQVR4nO3daVPaUBSAYTbFAAlCRCruihsVLW5dsdrNWlu1/f8/pn7jnMzkGjAhJLzvV2ZMHm+YyWG7mQwRERERUdDmdHGfTlRVX6t2VSlVz539ORRdL4uck9SiDwuirVJOtA06PYEGDTpdgQYNOl1NK7o2KL3oL13Z1w1R/9u+bLsqu4z7xF+Qe5SVLdUt0XqtLKo5sqsEr7u750HnB1nrtnyGr6mLvQU6WYEGDRo06CQGGjToaULX0oJ2s4YUOm+p8racucot2VU1bpY59zgwWldRF3tZrbsDevICDRo0aNCgQU9uoFOMdlUaXVQt1VUJRh9kj2XafD6zM2j2ItsblF2sJxntX/F8VnZRlA+mGD0zCDRo0KBBT06gQYMGnUS0GqsyJvT31Nx7d38cyQzo7M2qrKceSxa6bWJ611qmH0ov2hBo0KBBgwYNGjRo0M+k5qquiVI03G0nC91+K/tgMnfeyW6Si3bfm1ZMo2fUBL1qWOsUoXfUayWgQYMGDRo0aNCgQQ8HlU3JvXf7/U+Raa7yoDuyZE1ZbnCmV53ceXp0dPBAgwYNGjRo0KBBgx4KHfz+etLRbVl3z3C2PfUxQNP06On2o2wpfrR7G/TUi6tqZJ4Njv41ad/Ac98MgZYvjuwER0/cdy1BgwYNGjRo0KBBJwotuwsFbZnQrRDRB7KMRvdUHvTdJ9HvkdEV0dNKr4kKJ7JceGjTbyAWP6vV7ChYcdR52vNrkte2rCRrRbVV3DNo+bztjPxKgQkdx++GggYNGjRo0KBBgwY9HrS+jTej/QeO0dGW/35ZhajQVbUVsm3V/VscdZLK+/9N69QW28HVttRclctFhVYb79lW3rf64qir6f8389ap2ppgywMFDRo0aNCgQYMGDTpNaLUPsGfDqijQ+hCnaq7yoKPalbjaUls+b6gq6mxfjZo2z6tD3KvD7yvz9oPagDo0c6bqyMOUCupi1+hwsvrqgl4wXNCR7TTuQZfHjC6DBg0aNGjQoEGDnk701bJMbQNsV6wI6hfkMTS6Ic/FOYkK/bCretyU9ecj6F4dQs1Vjb/6bCIye9+1bKmLvRZF+/4XdKMZ0Schzf8BjV4rhF950/9J/IQeExQ0aNCgQYMGDRr0RKH1zGXLD9apN71e0OOyb0486BXVmerfQig1V/zbjQPtmbl0zUYphJxL0zHiMJt6Qvs/G4PnXMYNGSbQoEGDBg06IYEGnWq00wihVrLQK81QmoDN64bINB0NUdwMIiIiIhpL/wGJY9+ssJDbRQAAAABJRU5ErkJggg==");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<String> message = gson.fromJson(rest.createPost(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось загрузить изображение", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("Profile change error test")
    @Test
    void profileChangeErrorTest() {
        JsonObject json = new JsonObject();

        json.addProperty("id", 1);
        json.addProperty("identificator", "user123");
        json.addProperty("name", "someGoodName");
        json.addProperty("info", "No info");
        json.addProperty("imagePath", "profilePictures");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<Double> message = gson.fromJson(rest.changeProfile(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Идентификатор не может начинаться с 'user'", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void verifyLoginErrorTest(){
        JsonObject json = new JsonObject();

        json.addProperty("login", "freefree");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Dotenv wrongDotenv = Dotenv.configure().filename(".testEnv").load();
        RestapiController invalidRest = new RestapiController(wrongDotenv);

        Message<Double> message = gson.fromJson(invalidRest.verifyLogin(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось проверить доступность логина", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("Correct return of getPosts test")
    @Test
    void getPostsTest() throws JSONException {
        JsonObject json = new JsonObject();
        JsonObject json2 = new JsonObject();

        json.addProperty("authorId", 1);
        json.addProperty("categoryId", 2);
        json.addProperty("text", "text");
        json.addProperty("image", "");

        json2.addProperty("authorId", 1);
        json2.addProperty("categoryId", 3);
        json2.addProperty("text", "another text");
        json2.addProperty("image", "");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        String[] way = new String[4];
        Integer[] postId = new Integer[4];
        String[] postTime = new String[4];

        Message<String> message = gson.fromJson(rest.createPost(json.toString()), Message.class);

        assertEquals("Success", message.getState());

        org.json.JSONObject obj = new JSONObject(message.getData());
        way[0] = obj.getString("imagePath");
        postId[0] = obj.getInt("postId");
        postTime[0] = obj.getString("postTime");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        message = gson.fromJson(rest.createPost(json2.toString()), Message.class);

        assertEquals("Success", message.getState());

        obj = new JSONObject(message.getData());
        way[1] = obj.getString("imagePath");
        postId[1] = obj.getInt("postId");
        postTime[1] = obj.getString("postTime");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        message = gson.fromJson(rest.createPost(json.toString()), Message.class);

        assertEquals("Success", message.getState());

        obj = new JSONObject(message.getData());
        way[2] = obj.getString("imagePath");
        postId[2] = obj.getInt("postId");
        postTime[2] = obj.getString("postTime");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        message = gson.fromJson(rest.createPost(json2.toString()), Message.class);

        assertEquals("Success", message.getState());

        obj = new JSONObject(message.getData());
        way[3] = obj.getString("imagePath");
        postId[3] = obj.getInt("postId");
        postTime[3] = obj.getString("postTime");

        message = gson.fromJson(rest.getPosts(2, 0, 0), Message.class);

        Post[] posts = gson.fromJson(message.getData(), Post[].class);

        assertEquals("Success", message.getState());
        assertTrue(Arrays.stream(posts).count() < 4);
        assertTrue(posts[0].getTime().after(posts[1].getTime()));
        assertTrue(posts[1].getTime().after(posts[2].getTime()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss aaa", Locale.ENGLISH);

        for (int i = 0; i < 3; i++) {
            assertEquals(posts[i].getId(), postId[3 - i]);
            assertEquals(dateFormat.format(posts[i].getTime()), postTime[3 - i]);
            assertEquals(posts[i].getImage(), way[3 - i]);

            assertEquals(posts[i].getAuthorImage(), "profilePictures/avatar.jpg");
            assertEquals(posts[i].getIdentificator(), "user1");
            assertEquals(posts[i].getName(), "testUser1");

            if (i % 2 == 0) {
                assertEquals(posts[i].getText(), "another text");
                assertEquals(posts[i].getCategory(), "Кино");
            } else {
                assertEquals(posts[i].getText(), "text");
                assertEquals(posts[i].getCategory(), "Игры");
            }

            assertEquals(posts[i].getLikesCount(), 0);
            assertFalse(posts[i].isLiked());
        }
    }

    @DisplayName("Delete comment with post deletion")
    @Test
    void deleteCommentWithPostDeletion(){
        JsonObject json0 = new JsonObject();
        JsonObject json1 = new JsonObject();

        json0.addProperty("authorId", 1);
        json0.addProperty("categoryId", 2);
        json0.addProperty("text", "text");
        json0.addProperty("image", "");

        rest.createPost(json0.toString());

        json1.addProperty("userId", 1);
        json1.addProperty("postId", 1);
        json1.addProperty("commentText", "someText");

        rest.addComment(json1.toString());

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message message = gson.fromJson(rest.deletePost(1), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(1.0, message.getData());

        message = gson.fromJson(rest.getComments(1,0), Message.class);

        assertEquals("Success", message.getState());
        assertEquals("[]", message.getData());
    }

    @DisplayName("Notification create when tag in comment")
    @Test
    void addCommentWithTag() throws JSONException {
        JsonObject json0 = new JsonObject();
        JsonObject json1 = new JsonObject();

        json0.addProperty("authorId", 1);
        json0.addProperty("categoryId", 2);
        json0.addProperty("text", "text");
        json0.addProperty("image", "");

        rest.createPost(json0.toString());

        json1.addProperty("userId", 1);
        json1.addProperty("postId", 1);
        json1.addProperty("commentText", "someText @user2");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<String> message = gson.fromJson(rest.addComment(json1.toString()), Message.class);

        org.json.JSONObject obj = new JSONObject(message.getData());
        int commentId = obj.getInt("commentId");
        String name = obj.getString("name");
        String commentTime = obj.getString("commentTime");

        assertEquals("Success", message.getState());
        assertEquals(1, commentId);
        assertEquals("testUser1", name);

        Message<String> message2 = gson.fromJson(rest.getComments(1, 0), Message.class);

        Comment[] comments = gson.fromJson(message2.getData(), Comment[].class);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss aaa", Locale.ENGLISH);

        assertEquals("Success", message2.getState());
        assertEquals(commentId, comments[0].getId());
        assertEquals(1, comments[0].getAuthorId());
        assertEquals("user1", comments[0].getIdentificator());
        assertEquals(name, comments[0].getName());
        assertEquals("someText @user2", comments[0].getText());
        assertEquals("profilePictures/avatar.jpg", comments[0].getImagePath());
        assertEquals(commentTime, dateFormat.format(comments[0].getTime()));

        message2 = gson.fromJson(rest.getNotifications(2,0), Message.class);

        Notification[] notifications = gson.fromJson(message2.getData(), Notification[].class);

        assertEquals(1, notifications[0].getId());
        assertEquals(2, notifications[0].getUserId());
        assertEquals(1, notifications[0].getPostId());
    }

    @DisplayName("Edit profile correctly")
    @Test
    void editProfileCorrect(){
        JsonObject json = new JsonObject();

        json.addProperty("id", 1);
        json.addProperty("identificator", "newCoolIdent");
        json.addProperty("name", "someGoodName");
        json.addProperty("info", "No info");
        json.addProperty("imagePath", "iVBORw0KGgoAAAANSUhEUgAAAPQAAADPCAMAAAD1TAyiAAAA+VBMVEX39/f6AAAncyz6ooJieBkFAwMAAwP/AAD+/P7/pIYjcy1leBfsnnlUdQkAaAze5d6fh0dcdxP6p4b3//9JdiMbbyH6UEF9oX/6knX6blgAZgAUbRuiuqPSAQFddxzQlWiAfzE+dSZSdiBOhVH4z88eAwPD0cP5Z2cydCr38vKTAgLIzbpbcgD42Nj5XFz6HR36Pz/wAQE/fkOyjFSwuZtoAwOqAgL5iYmLAgL4qKj5e3v6QzYtAwOQgz3R29J8AwMAWwBGbwD34ODArJBwfTJdAwP6UFDhAQH4srL6LiX6clsUAwM7AwNPaQBbeS3o7OgvbQpqlmxyj1iAOB7VAAADtUlEQVR4nO3daVPaUBSAYTbFAAlCRCruihsVLW5dsdrNWlu1/f8/pn7jnMzkGjAhJLzvV2ZMHm+YyWG7mQwRERERUdDmdHGfTlRVX6t2VSlVz539ORRdL4uck9SiDwuirVJOtA06PYEGDTpdgQYNOl1NK7o2KL3oL13Z1w1R/9u+bLsqu4z7xF+Qe5SVLdUt0XqtLKo5sqsEr7u750HnB1nrtnyGr6mLvQU6WYEGDRo06CQGGjToaULX0oJ2s4YUOm+p8racucot2VU1bpY59zgwWldRF3tZrbsDevICDRo0aNCgQU9uoFOMdlUaXVQt1VUJRh9kj2XafD6zM2j2ItsblF2sJxntX/F8VnZRlA+mGD0zCDRo0KBBT06gQYMGnUS0GqsyJvT31Nx7d38cyQzo7M2qrKceSxa6bWJ611qmH0ov2hBo0KBBgwYNGjRo0M+k5qquiVI03G0nC91+K/tgMnfeyW6Si3bfm1ZMo2fUBL1qWOsUoXfUayWgQYMGDRo0aNCgQQ8HlU3JvXf7/U+Raa7yoDuyZE1ZbnCmV53ceXp0dPBAgwYNGjRo0KBBgx4KHfz+etLRbVl3z3C2PfUxQNP06On2o2wpfrR7G/TUi6tqZJ4Njv41ad/Ac98MgZYvjuwER0/cdy1BgwYNGjRo0KBBJwotuwsFbZnQrRDRB7KMRvdUHvTdJ9HvkdEV0dNKr4kKJ7JceGjTbyAWP6vV7ChYcdR52vNrkte2rCRrRbVV3DNo+bztjPxKgQkdx++GggYNGjRo0KBBgwY9HrS+jTej/QeO0dGW/35ZhajQVbUVsm3V/VscdZLK+/9N69QW28HVttRclctFhVYb79lW3rf64qir6f8389ap2ppgywMFDRo0aNCgQYMGDTpNaLUPsGfDqijQ+hCnaq7yoKPalbjaUls+b6gq6mxfjZo2z6tD3KvD7yvz9oPagDo0c6bqyMOUCupi1+hwsvrqgl4wXNCR7TTuQZfHjC6DBg0aNGjQoEGDnk701bJMbQNsV6wI6hfkMTS6Ic/FOYkK/bCretyU9ecj6F4dQs1Vjb/6bCIye9+1bKmLvRZF+/4XdKMZ0Schzf8BjV4rhF950/9J/IQeExQ0aNCgQYMGDRr0RKH1zGXLD9apN71e0OOyb0486BXVmerfQig1V/zbjQPtmbl0zUYphJxL0zHiMJt6Qvs/G4PnXMYNGSbQoEGDBg06IYEGnWq00wihVrLQK81QmoDN64bINB0NUdwMIiIiIhpL/wGJY9+ssJDbRQAAAABJRU5ErkJggg==");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<String> message = gson.fromJson(rest.changeProfile(json.toString()), Message.class);

        assertEquals("Success", message.getState());

        Path path = Paths.get("");

        String filePath = path.toAbsolutePath().toString();

        char delimitter;

        if(filePath.charAt(0)=='/'){
            delimitter = '/';
        } else {
            delimitter = '\\';
        }

        filePath = filePath.substring(0, filePath.indexOf(delimitter+"server"));

        File img = new File(filePath + delimitter + "client" + delimitter
                + "public" + delimitter + message.getData());
        
        String path2 = message.getData();

        assertTrue(img.exists());

        message = gson.fromJson(rest.getProfile(1), Message.class);

        Profile profile = gson.fromJson(message.getData(), Profile.class);

        assertEquals("Success", message.getState());
        assertEquals("newCoolIdent", profile.getIdentificator());
        assertEquals("someGoodName", profile.getName());
        assertEquals(0, profile.getFollows());
        assertEquals(0, profile.getFollowers());
        assertEquals("No info", profile.getInfo());
        assertEquals(path2, profile.getImagePath());

        img.delete();
    }

    @DisplayName("Comments return check")
    @Test
    void checkCommentOrder(){
        JsonObject json0 = new JsonObject();
        JsonObject json1 = new JsonObject();

        json0.addProperty("authorId", 1);
        json0.addProperty("categoryId", 2);
        json0.addProperty("text", "text");
        json0.addProperty("image", "");

        rest.createPost(json0.toString());

        json1.addProperty("userId", 1);
        json1.addProperty("postId", 1);
        json1.addProperty("commentText", "someText");

        for(int i = 0; i < 6; i++){
            rest.addComment(json1.toString());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<String> message = gson.fromJson(rest.getComments(1, 0), Message.class);

        Comment[] comments = gson.fromJson(message.getData(), Comment[].class);

        for(int i = 1; i < 6; i++) {
            assertEquals(i, comments[i - 1].getId());
            assertEquals(1, comments[i - 1].getAuthorId());
            assertEquals("user1", comments[i - 1].getIdentificator());
            assertEquals("testUser1", comments[i - 1].getName());
            assertEquals("someText", comments[i - 1].getText());
            assertEquals("profilePictures/avatar.jpg", comments[i - 1].getImagePath());
        }

        assertEquals("Success", message.getState());
        assertTrue(comments[0].getTime().before(comments[1].getTime()));
        assertTrue(comments[1].getTime().before(comments[2].getTime()));
        assertTrue(comments[2].getTime().before(comments[3].getTime()));
        assertTrue(comments[3].getTime().before(comments[4].getTime()));

        assertTrue(Arrays.stream(comments).count() == 5);
    }

    @DisplayName("Create notification after add tag into comment")
    @Test
    void addTagIntoComment(){
        JsonObject json0 = new JsonObject();
        JsonObject json1 = new JsonObject();

        json0.addProperty("authorId", 1);
        json0.addProperty("categoryId", 2);
        json0.addProperty("text", "text");
        json0.addProperty("image", "");

        rest.createPost(json0.toString());

        json1.addProperty("userId", 1);
        json1.addProperty("postId", 1);
        json1.addProperty("commentText", "someText");

        rest.addComment(json1.toString());

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<Double> message = gson.fromJson(rest.changeComment(1, "someText @user2"), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(1.0, message.getData());

        Message<String> message2 = gson.fromJson(rest.getComments(1, 0), Message.class);

        Comment[] comments = gson.fromJson(message2.getData(), Comment[].class);

        assertEquals("Success", message2.getState());
        assertEquals(1, comments[0].getId());
        assertEquals(1, comments[0].getAuthorId());
        assertEquals("user1", comments[0].getIdentificator());
        assertEquals("testUser1", comments[0].getName());
        assertEquals("someText @user2", comments[0].getText());
        assertEquals(comments[0].getImagePath(), "profilePictures/avatar.jpg");


        message2 = gson.fromJson(rest.getNotifications(2,0), Message.class);

        Notification[] notifications = gson.fromJson(message2.getData(), Notification[].class);

        assertEquals(1, notifications[0].getId());
        assertEquals(2, notifications[0].getUserId());
        assertEquals(1, notifications[0].getPostId());
    }
}
