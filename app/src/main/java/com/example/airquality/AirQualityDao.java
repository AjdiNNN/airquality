package com.example.airquality;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
@Dao
public interface AirQualityDao {

    @Insert
    void add(AirQuality airquality);

    @Query("SELECT * FROM air")
    List<AirQuality> getAll();

    @Query("SELECT * FROM air WHERE id = :id LIMIT 1")
    AirQuality getById(long id);

    @Query("SELECT * FROM air ORDER BY ID DESC LIMIT 1")
    AirQuality getLastEntry();

    @Query("SELECT * FROM air WHERE cityName = :cityName ORDER BY ID DESC LIMIT 1")
    AirQuality getByCityName(String cityName);
}
