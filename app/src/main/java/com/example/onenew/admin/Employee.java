package com.example.onenew.admin;

public class Employee {
    private final String id;
    private final String name;
    private final String password;

//    private final String dutyStartTime;
//    private final String dutyOffTime;
//    private final String breakStartTime;
//    private final String breakEndTime;
    private final String phone;
    private final String address;
    private final String photoUrl;   // 🔄 Cloudinary URL instead of int

    public Employee(String id, String name, String password,
//                    String dutyStartTime, String dutyOffTime,

//                    String breakStartTime, String breakEndTime,
                    String phone, String address, String photoUrl)
    {
        this.id = id;
        this.name = name;
        this.password = password;
//        this.status = status;
//        this.dutyStartTime = dutyStartTime;
//        this.dutyOffTime = dutyOffTime;
//        this.breakStartTime = breakStartTime;
//        this.breakEndTime = breakEndTime;
        this.phone = phone;
        this.address = address;
        this.photoUrl = photoUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    public String getPassword() { return password; }

    //    public String getStatus() { return status; }
//    public String getDutyStartTime() { return dutyStartTime; }
//    public String getDutyOffTime() { return dutyOffTime; }
//    public String getBreakStartTime() { return breakStartTime; }
//    public String getBreakEndTime() { return breakEndTime; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getPhotoUrl() { return photoUrl; }
}
