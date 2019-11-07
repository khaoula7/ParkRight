package com.charikati.parkright.model;

public class ViolationReport {
    //Type of violation
    private String type;
    //Image resource ID for the violation
    private int imageResourceId;
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
    // Date of sending violation
    private String sendingDate;
    // Time of sending violation
    private String sendingTime;
    //Reason of rejection
    private String declineReason;

    /**
     * Constructors
     */

    public ViolationReport(String type, int imageResourceId) {
        this.type = type;
        this.imageResourceId = imageResourceId;
    }

    public ViolationReport(String type, String status, String firstImageUrl, String secondImageUrl, String thirdImageUrl, double latitude, double longitude, String sendingDate, String sendingTime, String declineReason) {
        this.type = type;
        this.status = status;
        this.firstImageUrl = firstImageUrl;
        this.secondImageUrl = secondImageUrl;
        this.thirdImageUrl = thirdImageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sendingDate = sendingDate;
        this.sendingTime = sendingTime;
        this.declineReason = declineReason;
    }

    public ViolationReport(){}


    /**
     * Getter methods
     */
    public String getType() { return type; }

    public int getImageResourceId() {return imageResourceId;}

    public String getStatus() { return status; }

    public String getFirstImageUrl() { return firstImageUrl; }

    public String getSecondImageUrl() { return secondImageUrl; }

    public String getThirdImageUrl() { return thirdImageUrl; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public String getSendingDate() { return sendingDate; }

    public String getSendingTime() { return sendingTime; }

    public String getDeclineReason() { return declineReason; }


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

    public void setSendingDate(String sendingDate) {
        this.sendingDate = sendingDate;
    }

    public void setSendingTime(String sendingTime) {
        this.sendingTime = sendingTime;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

}
