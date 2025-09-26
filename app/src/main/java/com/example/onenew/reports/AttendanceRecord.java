package com.example.onenew.reports;

public class AttendanceRecord {
    public String employeeId, name, checkInTime, checkOutTime, status, breakStart, breakEnd;
    public long dutyMillis, breakMillis;
    public String date;

    public AttendanceRecord() { } // Firebase requires empty constructor

    public AttendanceRecord(String employeeId, String name, String checkInTime, String checkOutTime,
                            String status, String breakStart, String breakEnd, long dutyMillis, long breakMillis, String date) {
        this.employeeId = employeeId;
        this.name = name;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.breakStart = breakStart;
        this.breakEnd = breakEnd;
        this.dutyMillis = dutyMillis;
        this.breakMillis = breakMillis;
        this.date = date;
    }
}
