package com.example.controller;

import com.example.pojo.Coor;
import com.example.service.IDataService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/home")
public class centerController {
    @Resource
    private IDataService dataService;

    public double setAcc(double num){
        BigDecimal tmp = new BigDecimal(num);
        double res = tmp.setScale(4,BigDecimal.ROUND_HALF_UP).doubleValue();
        return res;
    }

    @RequestMapping("/findsmoke")
    @ResponseBody
    public Map<String,Object> findSmoking(HttpServletRequest request){
        String tmplat = request.getParameter("lat");
        String tmplng = request.getParameter("lng");
        double lat = Double.parseDouble(tmplat);
        double lng = Double.parseDouble(tmplng);
        lat = setAcc(lat);
        lng = setAcc(lng);
        long curtime = System.currentTimeMillis()/1000;
        long querytime = curtime-900;
        double range = 0.003;
        List<Coor> res = dataService.getNearbySmokingPoint(lat-range,lat+range,lng-range,lng+range,querytime);
        Map<String,Object> map= new HashMap<>();
        map.put("ans",res);
        return map;
    }

    @RequestMapping("/setsmoke")
    @ResponseBody
    public Map<String,Object> setSmoking(HttpServletRequest request){
        String tmplat = request.getParameter("lat");
        String tmplng = request.getParameter("lng");
        double lat = Double.parseDouble(tmplat);
        double lng = Double.parseDouble(tmplng);
        lat = setAcc(lat);
        lng = setAcc(lng);
        long curtime = System.currentTimeMillis()/1000;
        long querytime = 0;
        double range = 0.0001;
        Map<String, Object> map = new HashMap<>();
        map.put("result","success");
        List<Coor> tmp = dataService.getNearbySmokingPoint(lat-range,lat+range,lng-range,lng+range,querytime);
        if (tmp.size()==0){
            dataService.insertSmokingPoint(curtime,lat,lng);
        }
        else{
            dataService.updateSmokingPoint(curtime,lat,lng);
        }
        return map;
    }

    @RequestMapping("/findallergy")
    @ResponseBody
    public Map<String,Object> findAllergy(HttpServletRequest request){
        String tmplat = request.getParameter("lat");
        String tmplng = request.getParameter("lng");
        double lat = Double.parseDouble(tmplat);
        double lng = Double.parseDouble(tmplng);
        lat = setAcc(lat);
        lng = setAcc(lng);
        double range = 0.003;
        List<Coor> res = dataService.getNearbyAllergyPoint(lat-range,lat+range,lng-range,lng+range);
        Map<String,Object> map = new HashMap<>();
        map.put("ans",res);
        return map;
    }

    @RequestMapping("/setallergy")
    @ResponseBody
    public Map<String,Object> setAllergy(HttpServletRequest request){
        String tmplat = request.getParameter("lat");
        String tmplng = request.getParameter("lng");
        double lat = Double.parseDouble(tmplat);
        double lng = Double.parseDouble(tmplng);
        lat = setAcc(lat);
        lng = setAcc(lng);
        long curtime = System.currentTimeMillis()/1000;
        double range = 0.0001;
        Map<String, Object> map = new HashMap<>();
        map.put("result","success");
        List<Coor> tmp = dataService.getNearbyAllergyPoint(lat-range,lat+range,lng-range,lng+range);
        if (tmp.size()==0){
            dataService.insertAllergyPoint(curtime,lat,lng);
        }
        else{
            dataService.updateAllergyPoint(curtime,lat,lng);
        }
        return map;
    }
}
