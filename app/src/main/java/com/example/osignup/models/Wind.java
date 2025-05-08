package com.example.osignup.models;

import com.google.gson.annotations.SerializedName;

public class Wind {
    @SerializedName("speed")
    private float speed;

    @SerializedName("deg")
    private int degree;

    // Getters and setters
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }
}