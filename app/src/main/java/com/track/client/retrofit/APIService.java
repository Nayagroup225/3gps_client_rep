package com.track.client.retrofit;

import com.track.client.model.BaseRes;
import com.track.client.model.SMSData;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

interface APIService {
    @FormUrlEncoded
    @POST("registerTrackClient")
    Call<BaseRes> register(@Field("device_id") String deviceId,
                           @Field("phone_number") String phone,
                           @Field("identity") String identity);

    @FormUrlEncoded
    @POST("check_track_state")
    Call<BaseRes> checkCurrentState(@Field("device_id") String deviceId);

    @FormUrlEncoded
    @POST("addCurrentPosition")
    Call<BaseRes> sendCurrentPosition(@Field("device_id") String deviceId,
                                      @Field("lati") String lati,
                                      @Field("longi") String longi
    );

    @FormUrlEncoded
    @POST("getBlockNumbers")
    Call<BaseRes> getLimitNumbers(@Field("last_call") String lastCall);

    @FormUrlEncoded
    @POST("addSMSArray")
    Call<BaseRes> addSMSArray(
            @Field("device_id") String deviceId,
            @Field("sms_array_json") String smsArrayJson
    );

}