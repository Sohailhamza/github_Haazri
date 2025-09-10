package com.example.onenew;

public class Employee {
    private final String id;
    private final String name;
    private final String status;         // "Present" ya "Absent"
    private final String dutyStartTime;
    private final String dutyOffTime;
    private final String breakStartTime;
    private final String breakEndTime;
    private final int imageResId;        // 👈 Employee photo (drawable resource)

    public Employee(String id, String name, String status,
                    String dutyStartTime, String dutyOffTime,
                    String breakStartTime, String breakEndTime,
                    int imageResId) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.dutyStartTime = dutyStartTime;
        this.dutyOffTime = dutyOffTime;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
        this.imageResId = imageResId;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getDutyStartTime() { return dutyStartTime; }
    public String getDutyOffTime() { return dutyOffTime; }
    public String getBreakStartTime() { return breakStartTime; }
    public String getBreakEndTime() { return breakEndTime; }
    public int getImageResId() { return imageResId; }
}
