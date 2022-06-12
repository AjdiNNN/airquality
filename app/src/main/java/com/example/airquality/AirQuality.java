package com.example.airquality;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "air")
public class AirQuality { //todo_tasks
    @PrimaryKey(autoGenerate = true)
    private long id;
    private int AQIndex;
    private String cityName;
    private double latitude;
    private double longitude;
    private String iaqi;
    private String dateTaken;


    public AirQuality(int AQIndex, String cityName, double latitude, double longitude, String iaqi, String dateTaken) {
        this.AQIndex = AQIndex;
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.iaqi = iaqi;
        this.dateTaken = dateTaken;
    }

    @Ignore
    public AirQuality(long id, int AQIndex, String cityName, double latitude, double longitude, String iaqi, String dateTaken) {
        this.id = id;
        this.AQIndex = AQIndex;
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.iaqi = iaqi;
        this.dateTaken = dateTaken;
    }

    public AirQuality() {

    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAQIndex() {
        return AQIndex;
    }

    public void setAQIndex(int AQIndex) {
        this.AQIndex = AQIndex;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getIaqi() {
        return iaqi;
    }

    public void setIaqi(String iaqi) {
        this.iaqi = iaqi;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }
}
