package com.bigthree.restapi;

import com.bigthree.restapi.utils.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestapiControllerTest {

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private DataBase db;

    private RestapiController rest;

    @BeforeEach
    public void init() throws SQLException, ClassNotFoundException {
        rest = new RestapiController(db);
        lenient().when(db.initialSetUp()).thenReturn(null);
        lenient().when(db.getDBConnection()).thenReturn(connection);
        lenient().when(connection.createStatement()).thenReturn(statement);
        lenient().when(statement.execute(any(String.class))).thenReturn(true);
        lenient().when(statement.executeQuery(any(String.class))).thenReturn(resultSet);
    }

    @DisplayName("Success registration")
    @Test
    void regUserSuccess() throws SQLException {
        JsonObject json1 = new JsonObject();

        json1.addProperty("name", "testUser1");
        json1.addProperty("login", "testUser1");
        json1.addProperty("pass", "testPassword");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(false).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(1);

        Message<String> message = gson.fromJson(rest.regUser(json1.toString()), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(1.0, message.getData());
    }

    @DisplayName("Unsuccess login")
    @Test
    void regUserUnsuccessLoginNotFree() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("name", "testUser");
        json.addProperty("login", "testUser1");
        json.addProperty("pass", "testPassword");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true);

        Message<String> message = gson.fromJson(rest.regUser(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Логин занят", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("Login free")  //Причина и следствие
    @Test
    void verifyLoginFree() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("login", "testUser");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(false);

        Message<Boolean> message = gson.fromJson(rest.verifyLogin(json.toString()), Message.class);

        assertEquals("Success", message.getState());
        assertTrue(message.getData());
    }

    @DisplayName("Login not free")
    @Test
    void verifyLoginNotFree() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true);

        Message<Boolean> message = gson.fromJson(rest.verifyLogin("testUser1"), Message.class);

        assertEquals("Success", message.getState());
        assertFalse (message.getData());
    }

    @DisplayName("Login success")
    @Test
    void loginSuccess() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(any(String.class))).thenReturn(1);

        Message<Boolean> message = gson.fromJson(rest.loginUser("testUser1", "testPassword"), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(1.0, message.getData());
    }

    @DisplayName("Login unsuccess incorrect login")
    @Test
    void loginUnsuccessIncorrectLogin() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(false);

        Message<Boolean> message = gson.fromJson(rest.loginUser("wrongUser", "password"), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Неверный логин", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("Login unsuccess incorrect password")
    @Test
    void loginUnsuccessIncorrectPassword() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false);

        Message<Boolean> message = gson.fromJson(rest.loginUser("testUser1", "wrongpassword"), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Неверный пароль", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("Success follow")
    @Test
    void followUser() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("followerId", 2);
        json.addProperty("userId", 1);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();


        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(1);

        Message<Double> message = gson.fromJson(rest.followUser(json.toString()), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(1.0, message.getData());
    }

    @DisplayName("Right relationships test")
    @Test
    void getIsSubscribeRight() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true);

        Message<Boolean> message = gson.fromJson(rest.getIsSubscribe(2, 1), Message.class);

        assertEquals("Success", message.getState());
        assertTrue(message.getData());
    }

    @DisplayName("Success get profile")
    @Test
    void getProfile() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(0).thenReturn(1);
        when(resultSet.getString(any(String.class))).thenReturn("user1").thenReturn("testUser1").thenReturn("some").thenReturn("some");

        Message<String> message = gson.fromJson(rest.getProfile(1), Message.class);

        Profile profile = gson.fromJson(message.getData(), Profile.class);

        assertEquals("Success", message.getState());
        assertEquals("user1", profile.getIdentificator());
        assertEquals("testUser1", profile.getName());
        assertEquals(0, profile.getFollows());
        assertEquals(1, profile.getFollowers());
    }

    @DisplayName("Create post success without image")
    @Test
    void createPostSuccessWithOutImage() throws JSONException, SQLException {
        JsonObject json = new JsonObject();
        JsonObject json2 = new JsonObject();

        json.addProperty("authorId", 1);
        json.addProperty("categoryId", 2);
        json.addProperty("text", "text");
        json.addProperty("image", "");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(1);
        when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1873));

        Message<String> message = gson.fromJson(rest.createPost(json.toString()), Message.class);

        org.json.JSONObject obj = new JSONObject(message.getData());
        Integer id = obj.getInt("postId");

        assertEquals("Success", message.getState());
        assertEquals(1, id);
    }

    @DisplayName("Create post unsuccess")
    @Test
    void createPostWrongImage() {
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

    @DisplayName("Inc likes on post ")
    @Test
    void incLikesOnPostSuccess() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(false).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(1);

        Message<String> message = gson.fromJson(rest.incLikesOnPost(2,2), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(1.0, message.getData());
    }

    @DisplayName("Get post")
    @Test
    void getPostSuccess() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(any(String.class))).thenReturn(2).thenReturn(1).thenReturn(1);
        when(resultSet.getString(any(String.class))).thenReturn("profilepicture").thenReturn("identificator").thenReturn("name").thenReturn("text").thenReturn("Игры").thenReturn("");
        when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1873));

        Message<String> message = gson.fromJson(rest.getPost(2, 2), Message.class);

        Post post = gson.fromJson(message.getData(), Post.class);

        assertEquals("Success", message.getState());
        assertEquals(2, post.getId());
        assertEquals(1, post.getAuthorId());
        assertEquals("Игры", post.getCategory());
        assertEquals("", post.getImage());
        assertEquals("text", post.getText());
        assertEquals(1, post.getLikesCount());
        assertTrue(post.isLiked());
    }

    @DisplayName("Dec likes on post")
    @Test
    void decLikesOnPostSuccess() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false);

        Message<String> message = gson.fromJson(rest.decLikesOnPost(2,2), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(0.0, message.getData());
    }

    @DisplayName("Feed with only userId test") //Попарное тестирование
    @Test
    void getFeedWithUserId() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(2).thenReturn(1).thenReturn(1);
        when(resultSet.getString(any(String.class))).thenReturn("profilepicture").thenReturn("identificator").thenReturn("name").thenReturn("text").thenReturn("Игры").thenReturn("");
        when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1873));

        Message<String> message = gson.fromJson(rest.getPosts(2, 0,0), Message.class);

        Post[] posts = gson.fromJson(message.getData(), Post[].class);

        assertEquals("Success", message.getState());
        assertEquals("text", posts[0].getText());
    }

    @DisplayName("Feed with userId and prevPostId test") //Попарное тестирование
    @Test
    void getFeedWithPrevPostId() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(2).thenReturn(1).thenReturn(1);
        when(resultSet.getString(any(String.class))).thenReturn("profilepicture").thenReturn("identificator").thenReturn("name").thenReturn("text").thenReturn("Игры").thenReturn("");
        when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1873));

        Message<String> message = gson.fromJson(rest.getPosts(2, 4,0), Message.class);

        Post[] posts = gson.fromJson(message.getData(), Post[].class);

        assertEquals("Success", message.getState());
        assertTrue(posts[0].getAuthorId() != 2);
    }

    @DisplayName("Feed with UserId and category test") //Попарное тестирование
    @Test
    void getFeedWithCategory() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(false);
        Message<String> message = gson.fromJson(rest.getPosts(2, 0,3), Message.class);

        assertEquals("Success", message.getState());
        assertEquals("[]", message.getData());
    }


    @DisplayName("Feed with all parameters test") //Попарное тестирование
    @Test
    void getFeedWithAllParameters() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(2).thenReturn(1).thenReturn(1);
        when(resultSet.getString(any(String.class))).thenReturn("profilepicture").thenReturn("identificator").thenReturn("name").thenReturn("text").thenReturn("Игры").thenReturn("");
        when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1873));

        Message<String> message = gson.fromJson(rest.getPosts(2, 4,2), Message.class);

        Post[] posts = gson.fromJson(message.getData(), Post[].class);

        assertEquals("Success", message.getState());
        assertEquals("Игры", posts[0].getCategory());
    }

    @DisplayName("Success delete post test")
    @Test
    void deletePostSuccess() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString(any(String.class))).thenReturn("postpicture");

        Message<Double> message = gson.fromJson(rest.deletePost(4), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(1.0, message.getData());
    }

    @DisplayName("Subfeed with empty data test")
    @Test
    void getSubFeedNoPosts() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(false);
        Message<String> message = gson.fromJson(rest.getPosts(1, 0,0), Message.class);

        assertEquals("Success", message.getState());
        assertEquals("[]", message.getData());
    }

    @DisplayName("Subfeed with only userId test")
    @Test
    void getSubFeedWithUserId() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(2).thenReturn(1).thenReturn(1);
        when(resultSet.getString(any(String.class))).thenReturn("profilepicture").thenReturn("identificator").thenReturn("name").thenReturn("text").thenReturn("Игры").thenReturn("");
        when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1873));

        Message<String> message = gson.fromJson(rest.getSubPosts(2, 0,0), Message.class);

        Post[] posts = gson.fromJson(message.getData(), Post[].class);

        assertEquals("Success", message.getState());
        assertEquals(1, posts[0].getAuthorId());

    }

    @DisplayName("Subfeed with userId and prevPostId test")
    @Test
    void getSubFeedWithPrevPostId() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(2).thenReturn(1).thenReturn(1);
        when(resultSet.getString(any(String.class))).thenReturn("profilepicture").thenReturn("identificator").thenReturn("name").thenReturn("text").thenReturn("Игры").thenReturn("");
        when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1873));

        Message<String> message = gson.fromJson(rest.getPosts(2, 3,0), Message.class);

        Post[] posts = gson.fromJson(message.getData(), Post[].class);

        assertEquals("Success", message.getState());
        assertEquals(1, posts[0].getAuthorId());

    }

    @DisplayName("Subfeed with userId and category test")
    @Test
    void getSubFeedWithCategory() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(false);
        Message<String> message = gson.fromJson(rest.getPosts(2, 0,3), Message.class);

        Post[] posts = gson.fromJson(message.getData(), Post[].class);

        assertEquals("Success", message.getState());
        assertEquals("[]", message.getData());
    }

    @DisplayName("Subfeed with all parameters test")
    @Test
    void getSubFeedWithAllParameters() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(2).thenReturn(1).thenReturn(1);
        when(resultSet.getString(any(String.class))).thenReturn("profilepicture").thenReturn("identificator").thenReturn("name").thenReturn("text").thenReturn("Игры").thenReturn("");
        when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1873));

        Message<String> message = gson.fromJson(rest.getPosts(2, 3,2), Message.class);

        Post[] posts = gson.fromJson(message.getData(), Post[].class);

        assertEquals("Success", message.getState());;
        assertEquals("Игры", posts[0].getCategory());
        assertEquals(1, posts[0].getAuthorId());

    }

    @DisplayName("Empty data test")
    @Test
    void getFeedNoPosts() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(false);

        Message<String> message = gson.fromJson(rest.getPosts(1, 0,0), Message.class);

        assertEquals("Success", message.getState());
        assertEquals("[]", message.getData());
    }

    @DisplayName("Userposts with empty data test")
    @Test
    void getEmptyUserPosts() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(false);
        Message<String> message = gson.fromJson(rest.getUserPosts(2, 0), Message.class);

        assertEquals("Success", message.getState());
        assertEquals("[]", message.getData());
    }

    @DisplayName("Userposts with userId test")
    @Test
    void getUserPostsWithUserId() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(2).thenReturn(1).thenReturn(1);
        when(resultSet.getString(any(String.class))).thenReturn("profilepicture").thenReturn("identificator").thenReturn("name").thenReturn("text").thenReturn("Игры").thenReturn("");
        when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1873));

        Message<String> message = gson.fromJson(rest.getUserPosts(1, 0), Message.class);

        Post[] posts = gson.fromJson(message.getData(), Post[].class);

        assertEquals("Success", message.getState());
        assertEquals(1, posts[0].getAuthorId());
    }

    @DisplayName("Userposts with all parameters test")
    @Test
    void getUserPostsWithAll() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(2).thenReturn(1).thenReturn(1);
        when(resultSet.getString(any(String.class))).thenReturn("profilepicture").thenReturn("identificator").thenReturn("name").thenReturn("text").thenReturn("Игры").thenReturn("");
        when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1873));

        Message<String> message = gson.fromJson(rest.getUserPosts(1, 3), Message.class);

        Post[] posts = gson.fromJson(message.getData(), Post[].class);

        assertEquals("Success", message.getState());
        assertEquals(1, posts[0].getAuthorId());
    }

    @DisplayName("Wrong identificator test") //Негативное тестирование
    @Test
    void changeProfileWrongIdent() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("id", 1);
        json.addProperty("identificator", "user123");
        json.addProperty("name", "someGoodName");
        json.addProperty("info", "No info");
        json.addProperty("imagePath", "profilePictures");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(any(String.class))).thenReturn("user");

        Message<Double> message = gson.fromJson(rest.changeProfile(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Идентификатор не может начинаться с 'user'", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("Busy identificator test")
    @Test
    void changeProfileBusyIdent() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("id", 2);
        json.addProperty("identificator", "newCoolIdent");
        json.addProperty("name", "someGoodName");
        json.addProperty("info", "No info");
        json.addProperty("imagePath", "profilePictures");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(any(String.class))).thenReturn("busyUser");

        Message<Double> message = gson.fromJson(rest.changeProfile(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Данный идентификатор уже занят.", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("Change profile wrong image test")
    @Test
    void changeProfileWrongImage() {
        JsonObject json = new JsonObject();

        json.addProperty("id", 1);
        json.addProperty("identificator", "newCoolIdent");
        json.addProperty("name", "someGoodName");
        json.addProperty("info", "No info");
        json.addProperty("imagePath", "iVBORw0KGgoAAAANSUhEUgAAAPQAAADPCAMAAAD1TAyiAAAA+VBMVEX39/f6AAAncyz6ooJiFAwMAAwP/AAD+/P7/pIYjcy1leBfsnnlUdQkAaAze5d6fh0dcdxP6p4b3//9JdiMbbyH6UEF9oX/6knX6blgAZgAUbRuiuqPSAQFddxzQlWiAfzE+dSZSdiBOhVH4z88eAwPD0cP5Z2cydCr38vKTAgLIzbpbcgD42Nj5XFz6HR36Pz/wAQE/fkOyjFSwuZtoAwOqAgL5iYmLAgL4qKj5e3v6QzYtAwOQgz3R29J8AwMAWwBGbwD34ODArJBwfTJdAwP6UFDhAQH4srL6LiX6clsUAwM7AwNPaQBbeS3o7OgvbQpqlmxyj1iAOB7VAAADtUlEQVR4nO3daVPaUBSAYTbFAAlCRCruihsVLW5dsdrNWlu1/f8/pn7jnMzkGjAhJLzvV2ZMHm+YyWG7mQwRERERUdDmdHGfTlRVX6t2VSlVz539ORRdL4uck9SiDwuirVJOtA06PYEGDTpdgQYNOl1NK7o2KL3oL13Z1w1R/9u+bLsqu4z7xF+Qe5SVLdUt0XqtLKo5sqsEr7u750HnB1nrtnyGr6mLvQU6WYEGDRo06CQGGjToaULX0oJ2s4YUOm+p8racucot2VU1bpY59zgwWldRF3tZrbsDevICDRo0aNCgQU9uoFOMdlUaXVQt1VUJRh9kj2XafD6zM2j2ItsblF2sJxntX/F8VnZRlA+mGD0zCDRo0KBBT06gQYMGnUS0GqsyJvT31Nx7d38cyQzo7M2qrKceSxa6bWJ611qmH0ov2hBo0KBBgwYNGjRo0M+k5qquiVI03G0nC91+K/tgMnfeyW6Si3bfm1ZMo2fUBL1qWOsUoXfUayWgQYMGDRo0aNCgQQ8HlU3JvXf7/U+Raa7yoDuyZE1ZbnCmV53ceXp0dPBAgwYNGjRo0KBBgx4KHfz+etLRbVl3z3C2PfUxQNP06On2o2wpfrR7G/TUi6tqZJ4Njv41ad/Ac98MgZYvjuwER0/cdy1BgwYNGjRo0KBBJwotuwsFbZnQrRDRB7KMRvdUHvTdJ9HvkdEV0dNKr4kKJ7JceGjTbyAWP6vV7ChYcdR52vNrkte2rCRrRbVV3DNo+bztjPxKgQkdx++GggYNGjRo0KBBgwY9HrS+jTej/QeO0dGW/35ZhajQVbUVsm3V/VscdZLK+/9N69QW28HVttRclctFhVYb79lW3rf64qir6f8389ap2ppgywMFDRo0aNCgQYMGDTpNaLUPsGfDqijQ+hCnaq7yoKPalbjaUls+b6gq6mxfjZo2z6tD3KvD7yvz9oPagDo0c6bqyMOUCupi1+hwsvrqgl4wXNCR7TTuQZfHjC6DBg0aNGjQoEGDnk701bJMbQNsV6wI6hfkMTS6Ic/FOYkK/bCretyU9ecj6F4dQs1Vjb/6bCIye9+1bKmLvRZF+/4XdKMZ0Schzf8BjV4rhF950/9J/IQeExQ0aNCgQYMGDRr0RKH1zGXLD9apN71e0OOyb0486BXVmerfQig1V/zbjQPtmbl0zUYphJxL0zHiMJt6Qvs/G4PnXMYNGSbQoEGDBg06IYEGnWq00wihVrLQK81QmoDN64bINB0NUdwMIiIiIhpL/wGJY9+ssJDbRQAAAABJRU5ErkJggg==");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Message<Double> message = gson.fromJson(rest.changeProfile(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось загрузить изображение", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("Free identificator test")
    @Test
    void verifyIdentificatorFree() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(false);

        Message<Boolean> message = gson.fromJson(rest.verifyIdentificator("somefreeident", 0), Message.class);

        assertEquals("Success", message.getState());
        assertTrue(message.getData());
    }

    @DisplayName("Busy identificator test")
    @Test
    void verifyIdentificatorBusy() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true);

        Message<Boolean> message = gson.fromJson(rest.verifyIdentificator("user2", 0), Message.class);

        assertEquals("Success", message.getState());
        assertFalse(message.getData());
    }

    @DisplayName("Success unfollow")
    @Test
    void unfollowUser() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("followerId", 2);
        json.addProperty("userId", 1);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt(any(String.class))).thenReturn(0);

        Message<Double> message = gson.fromJson(rest.unfollowUser(json.toString()), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(0.0, message.getData());
    }

    @DisplayName("Success id by identificator")
    @Test
    void getIdByIdentificatorExists() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(any(String.class))).thenReturn(1);

        Message<Double> message = gson.fromJson(rest.getIdByIdentificator("user2"), Message.class);

        assertEquals("Success", message.getState());
        assertEquals(1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void changeProfileError() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("id", 1);
        json.addProperty("identificator", "newCoolIdent");
        json.addProperty("name", "someGoodName");
        json.addProperty("info", "No info");
        json.addProperty("imagePath", "iVBORw0KGgoAAAANSUhEUgAAAPQAAADPCAMAAAD1TAyiAAAA+VBMVEX39/f6AAAncyz6ooJieBkFAwMAAwP/AAD+/P7/pIYjcy1leBfsnnlUdQkAaAze5d6fh0dcdxP6p4b3//9JdiMbbyH6UEF9oX/6knX6blgAZgAUbRuiuqPSAQFddxzQlWiAfzE+dSZSdiBOhVH4z88eAwPD0cP5Z2cydCr38vKTAgLIzbpbcgD42Nj5XFz6HR36Pz/wAQE/fkOyjFSwuZtoAwOqAgL5iYmLAgL4qKj5e3v6QzYtAwOQgz3R29J8AwMAWwBGbwD34ODArJBwfTJdAwP6UFDhAQH4srL6LiX6clsUAwM7AwNPaQBbeS3o7OgvbQpqlmxyj1iAOB7VAAADtUlEQVR4nO3daVPaUBSAYTbFAAlCRCruihsVLW5dsdrNWlu1/f8/pn7jnMzkGjAhJLzvV2ZMHm+YyWG7mQwRERERUdDmdHGfTlRVX6t2VSlVz539ORRdL4uck9SiDwuirVJOtA06PYEGDTpdgQYNOl1NK7o2KL3oL13Z1w1R/9u+bLsqu4z7xF+Qe5SVLdUt0XqtLKo5sqsEr7u750HnB1nrtnyGr6mLvQU6WYEGDRo06CQGGjToaULX0oJ2s4YUOm+p8racucot2VU1bpY59zgwWldRF3tZrbsDevICDRo0aNCgQU9uoFOMdlUaXVQt1VUJRh9kj2XafD6zM2j2ItsblF2sJxntX/F8VnZRlA+mGD0zCDRo0KBBT06gQYMGnUS0GqsyJvT31Nx7d38cyQzo7M2qrKceSxa6bWJ611qmH0ov2hBo0KBBgwYNGjRo0M+k5qquiVI03G0nC91+K/tgMnfeyW6Si3bfm1ZMo2fUBL1qWOsUoXfUayWgQYMGDRo0aNCgQQ8HlU3JvXf7/U+Raa7yoDuyZE1ZbnCmV53ceXp0dPBAgwYNGjRo0KBBgx4KHfz+etLRbVl3z3C2PfUxQNP06On2o2wpfrR7G/TUi6tqZJ4Njv41ad/Ac98MgZYvjuwER0/cdy1BgwYNGjRo0KBBJwotuwsFbZnQrRDRB7KMRvdUHvTdJ9HvkdEV0dNKr4kKJ7JceGjTbyAWP6vV7ChYcdR52vNrkte2rCRrRbVV3DNo+bztjPxKgQkdx++GggYNGjRo0KBBgwY9HrS+jTej/QeO0dGW/35ZhajQVbUVsm3V/VscdZLK+/9N69QW28HVttRclctFhVYb79lW3rf64qir6f8389ap2ppgywMFDRo0aNCgQYMGDTpNaLUPsGfDqijQ+hCnaq7yoKPalbjaUls+b6gq6mxfjZo2z6tD3KvD7yvz9oPagDo0c6bqyMOUCupi1+hwsvrqgl4wXNCR7TTuQZfHjC6DBg0aNGjQoEGDnk701bJMbQNsV6wI6hfkMTS6Ic/FOYkK/bCretyU9ecj6F4dQs1Vjb/6bCIye9+1bKmLvRZF+/4XdKMZ0Schzf8BjV4rhF950/9J/IQeExQ0aNCgQYMGDRr0RKH1zGXLD9apN71e0OOyb0486BXVmerfQig1V/zbjQPtmbl0zUYphJxL0zHiMJt6Qvs/G4PnXMYNGSbQoEGDBg06IYEGnWq00wihVrLQK81QmoDN64bINB0NUdwMIiIiIhpL/wGJY9+ssJDbRQAAAABJRU5ErkJggg==");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.changeProfile(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось изменить информацию профиля", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test") //Предугадывание ошибок
    @Test
    void getProfileError() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.getProfile(1), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось загрузить профиль пользователя", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void getUserPostsError() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.getUserPosts(2, 0), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось загрузить посты пользователя", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void followUserError() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("followerId", 2);
        json.addProperty("userId", 1);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.followUser(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось подписаться на пользователя", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void getIsSubscribeError() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Boolean> message = gson.fromJson(rest.getIsSubscribe(2, 1), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось проверить подписку на пользователя", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void unfollowUserError() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("followerId", 2);
        json.addProperty("userId", 1);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.unfollowUser(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось отписаться от пользователя", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void verifyIdentificatorError() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.verifyIdentificator("somefreeident", 0), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось проверить доступность идентификатора", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void regUserUnsuccessError() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("name", "testUser");
        json.addProperty("login", "testUser");
        json.addProperty("pass", "testPassword");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.regUser(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось зарегистрировать пользователя", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void verifyLoginError() throws SQLException {
        JsonObject json = new JsonObject();

        json.addProperty("login", "freefree");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.verifyLogin(json.toString()), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось проверить доступность логина", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void loginUnsuccessError() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.loginUser("wrongUser", "password"), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось выполнить вход", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void getFeedWithError() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.getPosts(1, 6,2), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось загрузить посты новостной ленты", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("SQL error test")
    @Test
    void getPostUnSuccess() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<String> message = gson.fromJson(rest.getPost(3, 29), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось загрузить пост", message.getMessage());
    }

    @DisplayName("SQL error test")
    @Test
    void getSubFeedWithError() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<String> message = gson.fromJson(rest.getPosts(1, 6,2), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось загрузить посты новостной ленты", message.getMessage());
    }

    @DisplayName("SQL error test")
    @Test
    void getIdByIdentificatorError() throws SQLException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        SQLException ex = new SQLException();
        lenient().when(statement.execute(any(String.class))).thenThrow(ex);
        lenient().when(statement.executeQuery(any(String.class))).thenThrow(ex);

        Message<Double> message = gson.fromJson(rest.getIdByIdentificator("user3"), Message.class);

        assertEquals("Error", message.getState());
        assertEquals("Не удалось проверить идентификатор", message.getMessage());
        assertEquals(-1.0, message.getData());
    }

    @DisplayName("Main class test")
    @Test
    public void main(){
        RestapiApplication.main(new String[]{});
    }

    @DisplayName("CommentData class test")
    @Test
    public void commentDataTest(){
        CommentData commentData = new CommentData(1,1,"text");
        assertEquals(1, commentData.getUserId());
        assertEquals(1, commentData.getPostId());
        assertEquals("text", commentData.getCommentText());
    }

    @DisplayName("profileData class test")
    @Test
    public void profileDataTest(){
        ProfileData profileData = new ProfileData(1, "testUser", "testName", "info","");
        assertEquals(1, profileData.getId());
        assertEquals("testUser", profileData.getIdentificator());
        assertEquals("testName", profileData.getName());
        assertEquals("info", profileData.getInfo());
        assertEquals("", profileData.getImagePath());
    }

    @DisplayName("relationshipsData class test")
    @Test
    public void relationshipsDataTest(){
        RelationshipsData relationshipsData = new RelationshipsData(1,2);
        assertEquals(1, relationshipsData.getFollowerId());
        assertEquals(2, relationshipsData.getUserId());
    }

    @DisplayName("userData class test")
    @Test
    public void userDataTest(){
        UserData userData = new UserData("testName","testLogin", "testPass");
        assertEquals("testName", userData.getName());
        assertEquals("testLogin", userData.getLogin());
        assertEquals("testPass", userData.getPass());

    }
}