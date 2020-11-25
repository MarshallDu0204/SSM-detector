package com.example.service;

import com.example.dao.DataDao;
import com.example.pojo.Coor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("dataService")
public class serviceImpl implements IDataService{
    @Resource
    private DataDao dataDao;

    @Override
    public List<Coor> getNearbySmokingPoint(double lat1, double lat2, double lng1, double lng2, long time) {
        return this.dataDao.getNearbySmokingPoint(lat1,lat2,lng1,lng2,time);
    }

    @Override
    public List<Coor> getNearbyAllergyPoint(double lat1, double lat2, double lng1, double lng2) {
        return this.dataDao.getNearbyAllergyPoint(lat1,lat2,lng1,lng2);
    }

    @Override
    public void insertSmokingPoint(long time, double lat, double lng) {
        this.dataDao.insertSmokingPoint(1,time,lat,lng);
    }

    @Override
    public void updateSmokingPoint(long time, double lat, double lng) {
        this.dataDao.updateSmokingPoint(time,lat,lng,1);
    }

    @Override
    public void insertAllergyPoint(long time, double lat, double lng) {
        this.dataDao.insertAllergyPoint(2,time,lat,lng);
    }

    @Override
    public void updateAllergyPoint(long time, double lat, double lng) {
        this.dataDao.updateAllergyPoint(time,lat,lng,2);
    }
}
