package com.articlelate.restapi.utils;

public class Notification {
    private int id;
    private int userId;
    private int postId;

    public Notification(int id, int userId, int postId) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getPostId() { return postId; }
}
