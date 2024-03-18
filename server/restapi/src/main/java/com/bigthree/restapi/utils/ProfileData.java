package com.bigthree.restapi.utils;
public class ProfileData {
    private int id;
    private String identificator;
    private String name;
    private String info;
    private String imagePath;

    public ProfileData(int id, String identificator,
                       String name, String info, String imagePath) {
        this.id = id;
        this.identificator = identificator;
        this.name = name;
        this.info = info;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public String getIdentificator() {
        return identificator;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public String getImagePath() {
        return imagePath;
    }
}
