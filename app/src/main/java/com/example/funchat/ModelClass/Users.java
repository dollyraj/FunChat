package com.example.funchat.ModelClass;

public class Users {
    String status;
    String name;
    String email;
    String imageUri;
    String uid;


    public Users() {
    }

    public Users( String name, String email, String status,String imageUri, String uid) {
        this.status = status;
        this.name = name;
        this.email = email;
        this.imageUri = imageUri;
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
