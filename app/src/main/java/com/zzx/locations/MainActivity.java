package com.zzx.locations;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private EditText ipEt , portEt , idEt ,timeEt;
    private static SharedPreferences mSharedPreferences;
    private static final String Date="date";
    private static final String IP="IP";
    private static final String PORT="PORT";
    private static final String ID="ID";
    private static final String TIMES="TIMES";
    private TextView test;
    private Intent intent;
    private String ip;
    private String id;
    private String port;
    private String return_time;
    public static final String ACTION_GPSSERVICE = "action_gpsservice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences(Date,MODE_PRIVATE);
        ipEt = (EditText) findViewById(R.id.ip);
        portEt = (EditText) findViewById(R.id.port);
        idEt = (EditText) findViewById(R.id.id);
        timeEt = (EditText) findViewById(R.id.time);
        getUpdateData();
        intent = new Intent();
        intent.setClass(this,GpsService.class);
        if (!isServiceExisted(this,GpsService.class.getName())){
            startService(intent);
        }
    }
    public void getUpdateData(){
        String ip = mSharedPreferences.getString(IP, "");
        int port = mSharedPreferences.getInt(PORT, 19000);
        int time = mSharedPreferences.getInt(TIMES, 60);
        String id = mSharedPreferences.getString(ID, "");
        ipEt.setText(ip);
        portEt.setText(port+"");
        idEt.setText(id);
        timeEt.setText(time+"");
    }

    public void saveUpdateData(){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        ip = ipEt.getText().toString();
        id = idEt.getText().toString();
        port = portEt.getText().toString();
        return_time = timeEt.getText().toString();
        setString(IP,ip);
        setString(ID,id);
        setInt(PORT,Integer.parseInt(port));
        setInt(TIMES,Integer.parseInt(return_time));
        editor.putString(IP, ip);
        editor.putInt(PORT, Integer.parseInt(port));
        editor.putString(ID, id);
        editor.putInt(TIMES, Integer.parseInt(return_time));
        editor.commit();
    }
    public void setString(String name ,String value){
        Settings.System.putString(getContentResolver(),name,value);

    }
    public void setInt(String name ,int value){
        Settings.System.putInt(getContentResolver(),name,value);

    }

    public void OnSave(View view){
        if (ipEt.getText().toString().equals(ip)&& portEt.getText().toString().equals(port)
                &&idEt.getText().toString().equals(id)&&timeEt.getText().toString().equals(return_time)){
            return;
        }
        if (idEt.getText().toString()==null||idEt.getText().toString().length()<11){
            Toast.makeText(this,R.string.enter_the_correct_number,Toast.LENGTH_SHORT).show();
            return;
        }
        saveUpdateData();
        if (id.length()<11){
            Toast.makeText(this,R.string.enter_the_correct_number,Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.setAction(ACTION_GPSSERVICE);
        sendBroadcast(intent);
    }
    public static boolean isServiceExisted(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

