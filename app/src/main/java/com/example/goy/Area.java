package com.example.goy;

import androidx.annotation.NonNull;

public class Area {
    private double latitude;
    private double longitude;
    private float radius;
    private String name;

    public Area(double latitude, double longitude, float radius, String name){
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.name = name;
    }

    public double getLatitude(){return latitude;}
    public double getLongitude(){return longitude;}
    public float getRadius(){return radius;}
    public String getName(){return name;}
}

