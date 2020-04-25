package com.charikati.parkright.model;

public class MonthReportsData {
    String month;
    int reports;

    public MonthReportsData(String month, int reports) {
        this.month = month;
        this.reports = reports;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getReports() {
        return reports;
    }

    public void setReports(int reports) {
        this.reports = reports;
    }
}
