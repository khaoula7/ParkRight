package com.charikati.parkright;

public class ViolationReport {
    //Type of violation
    private String type;
    //Status of violation - pending, accepted or rejected -- initialized to pending
    private String status;
    //Images download Url
    private String firstImageUrl;
    private String secondImageUrl;
    private String thirdImageUrl;
    //Location latitude
    private double latitude;
    //Location longitude
    private double longitude;
    // Date and time of sending violation
    private String sendingTime;

    /**
     * Constructor
     */
    public ViolationReport(String type, String status, String firstImageUrl, String secondImageUrl, String thirdImageUrl, double latitude, double longitude, String sendingTime) {
        this.type = type;
        this.status = status;
        this.firstImageUrl = firstImageUrl;
        this.secondImageUrl = secondImageUrl;
        this.thirdImageUrl = thirdImageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sendingTime = sendingTime;
    }

    /**
     * Getter methods
     */
    public String getType() { return type; }

    public String getStatus() { return status; }

    public String getFirstImageUrl() { return firstImageUrl; }

    public String getSecondImageUrl() { return secondImageUrl; }

    public String getThirdImageUrl() { return thirdImageUrl; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public String getSendingTime() { return sendingTime; }

    /**
     * Setter methods
     */

    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFirstImageUrl(String firstImageUrl) {
        this.firstImageUrl = firstImageUrl;
    }

    public void setSecondImageUrl(String secondImageUrl) {
        this.secondImageUrl = secondImageUrl;
    }

    public void setThirdImageUrl(String thirdImageUrl) {
        this.thirdImageUrl = thirdImageUrl;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setSendingTime(String sendingTime) {
        this.sendingTime = sendingTime;
    }
}
