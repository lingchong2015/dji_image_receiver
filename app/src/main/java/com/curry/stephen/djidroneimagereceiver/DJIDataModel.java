package com.curry.stephen.djidroneimagereceiver;

public class DJIDataModel {

    private double lat;
    private double lon;
    private float alt;
    private float pitch;
    private float yaw;
    private float roll;
    private String distance;
    private String datetime;

    public DJIDataModel() {

    }

    public DJIDataModel(double lat, double lon, float alt, float pitch, float yaw, float roll, String distance, String datetime) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.distance = distance;
        this.datetime = datetime;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public float getAlt() {
        return alt;
    }

    public void setAlt(float alt) {
        this.alt = alt;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "DJIDataModel{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", alt=" + alt +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                ", roll=" + roll +
                ", distance='" + distance + '\'' +
                ", datetime='" + datetime + '\'' +
                '}';
    }
}
