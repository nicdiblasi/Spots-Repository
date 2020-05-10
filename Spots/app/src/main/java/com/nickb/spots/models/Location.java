package com.nickb.spots.models;

public class Location {
    private double latitude;
    private double longitude;
    private String feature;


    public Location(double latitude, double longitude, String feature) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.feature = feature;
    }


    public Location() {
    }


    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", feature='" + feature + '\'' +
                '}';
    }
}
