package com.example.goy;

public class Area {
    private final double latitude;
    private final double longitude;
    private final float radius;
    private final String name;

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

