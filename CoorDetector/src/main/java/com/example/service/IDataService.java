package com.example.service;

import com.example.pojo.Coor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IDataService {
    public List<Coor> getNearbySmokingPoint(@Param("lat1") double lat1, @Param("lat2") double lat2, @Param("lng1") double lng1, @Param("lng2") double lng2, @Param("time") long time);

    public List<Coor> getNearbyAllergyPoint(@Param("lat1") double lat1, @Param("lat2") double lat2, @Param("lng1") double lng1, @Param("lng2") double lng2);

    public void insertSmokingPoint(@Param("time") long time, @Param("lat") double lat, @Param("lng") double lng);

    public void updateSmokingPoint(@Param("time") long time, @Param("lat") double lat, @Param("lng") double lng);

    public void insertAllergyPoint(@Param("time") long time, @Param("lat") double lat, @Param("lng") double lng);

    public void updateAllergyPoint(@Param("time") long time, @Param("lat") double lat, @Param("lng") double lng);

}
