package com.clarimire.entity;

import lombok.Data;

@Data
public class CheckinRequest {
    private Double lat;
    private Double lng;
    private String address;
    private String inspector;
    private String time;

    // Getters and Setters
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getInspector() { return inspector; }
    public void setInspector(String inspector) { this.inspector = inspector; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
