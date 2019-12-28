package com.charikati.parkright.model;

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
    //Sending time in milliseconds
    private long sendingTime;
    //decision time in milliseconds
    private long decisionTime;
    //Reason of rejection
    private String declineReason;
    //Violation Ticket number
    private String ticket;
    // Car licence number
    private String licence;

    /**
     * Constructor
     */
    public ViolationReport(String type, String status, String firstImageUrl, String secondImageUrl, String thirdImageUrl,
                           double latitude, double longitude, long sendingTime, long decisionTime, String declineReason,
                           String ticket, String licence) {
        this.type = type;
        this.status = status;
        this.firstImageUrl = firstImageUrl;
        this.secondImageUrl = secondImageUrl;
        this.thirdImageUrl = thirdImageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sendingTime = sendingTime;
        this.decisionTime = decisionTime;
        this.declineReason = declineReason;
        this.ticket = ticket;
        this.licence = licence;
    }

    public ViolationReport(){}

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
    public long getSendingTime() { return sendingTime; }
    public long getDecisionTime() { return decisionTime; }
    public String getDeclineReason() { return declineReason; }
    public String getTicket() { return ticket; }
    public String getLicence() { return licence; }
}
