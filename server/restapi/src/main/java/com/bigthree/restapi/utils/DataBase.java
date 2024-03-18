package com.bigthree.restapi.utils;

import java.sql.*;
import java.util.Properties;

public class DataBase {

    static String DB_URL;
    static String USER;
    static String PASS;

    public DataBase(String DB_URL, String USER, String PASS) throws SQLException, ClassNotFoundException {
        this.DB_URL = DB_URL;
        this.USER = USER;
        this.PASS = PASS;

        //initialSetUp();
    }

    public String initialSetUp() throws SQLException, ClassNotFoundException{
        Connection dbConnection = null;
        Statement statement = null;

        String createTableSQL = "CREATE TABLE IF NOT EXISTS auth_data (\n" +
                "    id serial PRIMARY KEY,\n" +
                "    login character varying(16) NOT NULL,\n" +
                "    pass character varying(16) NOT NULL\n" +
                ");" +

                "CREATE TABLE IF NOT EXISTS user_info (\n" +
                "    id serial PRIMARY KEY,\n" +
                "    info character varying(1000),\n" +
                "    loginid integer REFERENCES auth_data(id) NOT NULL,\n" +
                "    name character varying(30) NOT NULL,\n" +
                "    identificator character varying(30) NOT NULL,\n" +
                "    profilepicture character varying(100) NOT NULL\n" +
                ");" +

                "CREATE TABLE IF NOT EXISTS categories (\n" +
                "    id integer PRIMARY KEY,\n" +
                "    name character varying(20) NOT NULL\n" +
                ");" +

                "INSERT INTO categories(id, name) VALUES(1, 'IT'), (2, 'Игры'), (3, 'Кино'), " +
                "(4, 'Арты'), (5, 'Юмор'), (6, 'Наука'), (7, 'Музыка'), (8, 'Новости') " +
                "ON CONFLICT(id) DO NOTHING;" +

                "CREATE TABLE IF NOT EXISTS posts (\n" +
                "    id serial PRIMARY KEY,\n" +
                "    authorid integer REFERENCES user_info(id) NOT NULL,\n" +
                "    posttime timestamp without time zone NOT NULL,\n" +
                "    postcategoryid integer REFERENCES categories(id) NOT NULL,\n" +
                "    postpicture character varying(100),\n" +
                "    posttext character varying(10000) NOT NULL,\n" +
                "    postlikes integer\n" +
                ");"+

                "CREATE TABLE IF NOT EXISTS commentaries (\n" +
                "    id serial PRIMARY KEY,\n" +
                "    userid integer REFERENCES user_info(id) NOT NULL,\n" +
                "    postid integer REFERENCES posts(id) ON DELETE CASCADE,\n" +
                "    commenttime timestamp without time zone NOT NULL,\n" +
                "    commenttext character varying(3000) NOT NULL\n" +
                ");"+

                "CREATE TABLE IF NOT EXISTS notifications (\n" +
                "    id serial PRIMARY KEY,\n" +
                "    userid integer REFERENCES user_info(id) NOT NULL,\n" +
                "    postid integer NOT NULL\n" +
                ");"+

                "CREATE TABLE IF NOT EXISTS relationships (\n" +
                "    followerid integer REFERENCES user_info(id) NOT NULL,\n" +
                "    subscribeid integer REFERENCES user_info(id) NOT NULL\n" +
                ");"+

                "CREATE TABLE IF NOT EXISTS likes (\n"+
                "   userid integer REFERENCES user_info (id),\n"+
                "   postid integer\n" +
                ");";

        dbConnection = getDBConnection();
        statement = dbConnection.createStatement();

        statement.execute(createTableSQL);

        if (statement != null) {
            statement.close();
        }
        if (dbConnection != null) {
            dbConnection.close();
        }
        return "Ok";
    }

    public Connection getDBConnection() throws ClassNotFoundException, SQLException {
        Connection dbConnection = null;

        Class.forName("org.postgresql.Driver");

        System.out.println("PostgreSQL JDBC Driver successfully connected");

        Properties props = new Properties();

        props.setProperty("user", USER);
        props.setProperty("password", PASS);
        props.setProperty("useUnicode","true");
        props.setProperty("characterEncoding","windows-1251");

        dbConnection = DriverManager.getConnection(DB_URL, props);

        return dbConnection;
    }
}