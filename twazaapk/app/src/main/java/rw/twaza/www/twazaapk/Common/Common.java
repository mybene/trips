package com.twaza.wwww.twazaapk.Common;

import com.twaza.wwww.twazaapk.Remote.IGoogleAPI;
import com.twaza.wwww.twazaapk.Remote.RetrofitClient;

public class Common
{
    public  static final  String baseURL ="https://maps.googleleapis.com";
    public  static IGoogleAPI getGoogleApi()
    {
        return RetrofitClient.getRetrofit(baseURL).create(IGoogleAPI.class);
    }

}
