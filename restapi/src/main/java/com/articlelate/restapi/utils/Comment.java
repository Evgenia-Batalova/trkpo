package com.articlelate.restapi.utils;

import java.sql.Timestamp;

public class Comment {
    private int id;
    private int authorId;
    private String identificator;
    private String name;
    private Timestamp time;
    private String text;
    private String imagePath;

    public Comment(int id, int authorId, String identificator, String name,
                   Timestamp time, String text, String imagePath ) {
        this.id = id;
        this.authorId = authorId;
        this.identificator = identificator;
        this.name = name;
        this.time = time;
        this.text = text;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public Timestamp getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

    public String getIdentificator() {
        return identificator;
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }
}
