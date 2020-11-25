package com.example.pojo;

public class Coor {
    private double lat;
    private double lng;

    public Coor(Double lat, Double lng){
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat(){
        return lat;
    }

    public double getLng(){
        return lng;
    }
}
