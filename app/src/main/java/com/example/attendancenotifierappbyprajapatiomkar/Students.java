package com.example.attendancenotifierappbyprajapatiomkar;

public class Students {
    private String Name;
    private String Percentage;

    public Students(String name, String percentage) {
        Name = name;
        Percentage = percentage;
    }

    public String getName() {
        return Name;
    }

    public String getPercentage() {
        return Percentage;
    }
}
