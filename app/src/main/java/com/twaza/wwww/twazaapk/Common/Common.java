package com.twaza.wwww.twazaapk.Common;

import android.location.Location;

import com.twaza.wwww.twazaapk.Remote.FCMClient;
import com.twaza.wwww.twazaapk.Remote.IFMCService;
import com.twaza.wwww.twazaapk.Remote.IGoogleAPI;
import com.twaza.wwww.twazaapk.Remote.RetrofitClient;
import com.twaza.wwww.twazaapk.mode.User;

public class Common
{
    public  static String currentToken="";
    public static  final String driver_tbl ="Drivers";
    public static  final String user_driver_tbl ="DriversInformation";
    public static  final String User_rider_tbl ="RidersInformation";
    public static  final String pickup_request_tbl ="pickupRequest";
    public static  final String tokeni_tbl ="Tokens";

    public  static  double base_fare =2.5;
    private  static   double time_rate = 0.35;
    private static  double distance_rate =1.75;
    public  static  double formulaprice (double km ,double min)
    {
        return base_fare+(distance_rate*km)+(time_rate*min);
    }

    public  static User currentUser;

  public static Location mLastLocation = null;

    public  static final  String baseURL ="https://maps.googleapis.com";
    public  static final  String fcmURL ="https://fcm.googleapis.com/";
    public  static IGoogleAPI getGoogleApi()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
    public  static IFMCService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFMCService.class);
    }
}
