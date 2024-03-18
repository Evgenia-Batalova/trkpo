package com.bigthree.restapi.utils;
public class RelationshipsData {
    private int followerId;
    private int userId;

    public RelationshipsData(int followerId, int userId) {
        this.followerId = followerId;
        this.userId = userId;
    }

    public int getFollowerId() {
        return followerId;
    }

    public int getUserId() {
        return userId;
    }
}
