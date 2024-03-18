package com.bigthree.restapi.utils;

public class CommentData {
    private int userId;
    private int postId;
    private String commentText;

    public CommentData(int userId, int postId, String commentText) {
        this.userId = userId;
        this.postId = postId;
        this.commentText = commentText;
    }

    public int getUserId() {
        return userId;
    }

    public int getPostId() {
        return postId;
    }

    public String getCommentText() {
        return commentText;
    }
}
