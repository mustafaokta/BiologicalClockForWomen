package com.applandeo.materialcalendarsampleapp.models;

import java.util.Calendar;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CalendarModel extends RealmObject {
    @PrimaryKey
    private String id;
    private Date convertedCalendertoDate;
    private String type;

    @Override
    public String toString() {
        return "CalendarModel{" +
                "id='" + id + '\'' +
                ", convertedCalendertoDate=" + convertedCalendertoDate.toString() +
                ", type='" + type + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getConvertedCalendertoDate() {
        return convertedCalendertoDate;
    }

    public void setConvertedCalendertoDate(Date convertedCalendertoDate) {
        this.convertedCalendertoDate = convertedCalendertoDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

