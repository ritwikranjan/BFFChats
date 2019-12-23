package com.ritwik.bffchats;

public class Messages {
    public String message;
    public String name;
    public String photoUrl;

    public Messages() {
    }

    public Messages(String message, String name, String photoUrl) {
        this.message = message;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
