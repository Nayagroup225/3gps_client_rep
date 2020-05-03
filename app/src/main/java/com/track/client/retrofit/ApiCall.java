package com.track.client.retrofit;

import com.track.client.model.BaseRes;
import com.track.client.model.SMSData;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiCall {
    private static APIService service;

    public static ApiCall getInstance() {
        if (service == null) {
            service = RestClient.getClient();
        }
        return new ApiCall();
    }


    public void registerWithId(String deviceId, String phone, String identity, final IApiCallback<BaseRes> iApiCallback) {
        Call<BaseRes> call = service.register(deviceId, phone, identity);
        call.enqueue(new Callback<BaseRes>() {
            @Override
            public void onResponse(Call<BaseRes> call, Response<BaseRes> response) {
                iApiCallback.onSuccess("register", response);
            }

            @Override
            public void onFailure(Call<BaseRes> call, Throwable t) {
                iApiCallback.onFailure("" + t.getMessage());
            }
        });
    }

    public void checkCurrentState(String deviceId, final IApiCallback<BaseRes> iApiCallback) {
        Call<BaseRes> call = service.checkCurrentState(deviceId);
        call.enqueue(new Callback<BaseRes>() {
            @Override
            public void onResponse(Call<BaseRes> call, Response<BaseRes> response) {
                iApiCallback.onSuccess("state", response);
            }

            @Override
            public void onFailure(Call<BaseRes> call, Throwable t) {
                iApiCallback.onFailure("" + t.getMessage());
            }
        });
    }

    public void sendCurrentPosition(String deviceId, String lati, String longi, final IApiCallback<BaseRes> iApiCallback) {
        Call<BaseRes> call = service.sendCurrentPosition(deviceId, lati, longi);
        call.enqueue(new Callback<BaseRes>() {
            @Override
            public void onResponse(Call<BaseRes> call, Response<BaseRes> response) {
                iApiCallback.onSuccess("add_location", response);
            }

            @Override
            public void onFailure(Call<BaseRes> call, Throwable t) {
                iApiCallback.onFailure("" + t.getMessage());
            }
        });
    }

    public void addSMSArray(String deviceId, String smsArrayJson, final IApiCallback<BaseRes> iApiCallback) {
        Call<BaseRes> call = service.addSMSArray(deviceId, smsArrayJson);
        call.enqueue(new Callback<BaseRes>() {
            @Override
            public void onResponse(Call<BaseRes> call, Response<BaseRes> response) {
                iApiCallback.onSuccess("add_sms", response);
            }

            @Override
            public void onFailure(Call<BaseRes> call, Throwable t) {
                iApiCallback.onFailure("" + t.getMessage());
            }
        });
    }

    public void getLimitNumbers(final IApiCallback<BaseRes> iApiCallback) {
        Call<BaseRes> call = service.getLimitNumbers("test");
        call.enqueue(new Callback<BaseRes>() {
            @Override
            public void onResponse(Call<BaseRes> call, Response<BaseRes> response) {
                iApiCallback.onSuccess("limit_numbers", response);
            }

            @Override
            public void onFailure(Call<BaseRes> call, Throwable t) {
                iApiCallback.onFailure("" + t.getMessage());
            }
        });
    }
}