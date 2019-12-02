package com.twaza.wwww.twazaapk.Remote;

import com.twaza.wwww.twazaapk.mode.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import com.twaza.wwww.twazaapk.mode.FCMResponse;

public interface IFMCService {
    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAA_mnXk6s:APA91bFSM1TdTUiFBVxtUVCvFU9kaSa25zntCUxacil8nRhUC57CNuUflR-hkiTlMwsfv2_a4N7ssxADDeX5qJXDTh38AUb1G4qR7-DMAXuVT2Z2DapcUEAHbvSd9GGeXJoYE7J0Otuo"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);

}
