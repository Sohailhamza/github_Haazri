package com.example.onenew.reports;

public class AttendanceRecord {
    private String date;
    private String status;
    private String dutyHours;
    private String overtime;
    private String shortTime;

    // Required empty constructor (Firestore ke liye)
    public AttendanceRecord() {
    }

    public AttendanceRecord(String date, String status, String dutyHours, String overtime, String shortTime) {
        this.date = date;
        this.status = status;
        this.dutyHours = dutyHours;
        this.overtime = overtime;
        this.shortTime = shortTime;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getDutyHours() {
        return dutyHours;
    }

    public String getOvertime() {
        return overtime;
    }

    public String getShortTime() {
        return shortTime;
    }
}
