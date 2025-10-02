package com.example.onenew;

public class EmployeeAttendance {
    public String name;
    public String employeeId;
    public String status;
    public String checkInTime;
    public String checkOutTime;
    public String breakStart;
    public String breakEnd;

    public String selfieUrl;
    public long breakMillis;
    public long dutyMillis;


    public EmployeeAttendance() {} // Firestore ke liye empty constructor

    public EmployeeAttendance(String name, String employeeId,
                              String status,
                              String checkInTime,
                              String checkOutTime,
                              String breakStart, String breakEnd,
                              String selfieUrl,

                              long breakMillis,
                              long dutyMillis) {
        this.name = name;
        this.employeeId = employeeId;
        this.status = status;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.breakStart = breakStart;
        this.breakEnd = breakEnd;
        this.breakMillis = breakMillis;
        this.dutyMillis = dutyMillis;
        this.selfieUrl = selfieUrl;
    }
}
