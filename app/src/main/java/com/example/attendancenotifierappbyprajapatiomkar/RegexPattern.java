package com.example.attendancenotifierappbyprajapatiomkar;

public class RegexPattern {
    private String NAME_REGEX = "([0-9]+[,][A-Z])\\w+(['\\s']+([A-Z])\\w+)+";
    private String EMAIL_REGEX = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";
    private String PERCENTAGE_REGEX = "[0-9][0-9]*[.][0-9]*";

    public String getNAME_REGEX() {
        return NAME_REGEX;
    }

    public String getEMAIL_REGEX() {
        return EMAIL_REGEX;
    }

    public String getPERCENTAGE_REGEX() {
        return PERCENTAGE_REGEX;
    }
}
