package com.zzx.locations;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.location.locationApplaction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by Administrator on 2018/1/4 0004.
 */

public class GpsService extends Service {
    private static final String TAG = "GpsService";
    private static Context mContext;
    private static final String Date = "date";
    private static final String IP = "IP";
    private static final String PORT = "PORT";
    private static final String ID = "ID";
    private static final String TIMES = "TIMES";
    //    public static final String GPS_IP = "171.217.92.216";
//    public static final String GPS_PORT = "32001";
    public static String GPS_IP = "125.67.64.225";
    public static String GPS_PORT = "19000";
    private Socket mSocketLocation;
    private int mPort;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private static LocationManager mLocationManager;
    private static LocationUtils locationUtils;
    private static Location mLocation;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private static TelephonyManager mTelephonyMgr;
    public static final String SIGN = "7E";//标识位
    private static String NEW_ID = "0200"; //消息ID
    private static String NEW_PROPERTY = "001C";//消息体属性
    private static String PHONE_NUMBER = "00000000000";// 手机号
    private static String NEW_SERIAL_NUMBER = "0000";// 消息流水号
    private static String HEADERS;//消息头
    private static String CHECK_CODE = "00"; //校验码
    private static String NEWS;//消息体
    private static int CALLBALK_TIME = 30000;//回调时间
    private static MyReceiver myReceiver;
    public static final String ACTION_GPSSERVICE = "action_gpsservice";
    private static String mLocationMassege;
    private static boolean isGetLocation = false;
    public static final int GPS_DATA = 0X55;
    public static final int RECONNECT = 0X56;


    @Override
    public void onCreate() {
        mContext = getApplicationContext();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null && intent.getAction() == null) {
            return START_NOT_STICKY;
        }

        //设置广播接受消息
        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter(ACTION_GPSSERVICE);
        registerReceiver(myReceiver, filter);
        getdates();
        startSocket(GPS_IP, GPS_PORT);
        locationUtils = new LocationUtils();
        //获取定位管理器
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //判断GPS是否正常启动
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //  Toast.makeText(this, "请开启GPS...", Toast.LENGTH_SHORT).show();
            //  locationUtils.openGpsSettings(mContext);
        }
        if (true) {
            /**走百度定位**/
            mLocation = locationApplaction.getLoation();
            Log.e(TAG, "onStartCommand: " + mLocation);
        } else {
            // 通过GPS获取定位的位置数据
            mLocation = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(getLocationProvider(), true));
            mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(getLocationProvider(), true), 1000, 1, mLocationListener);
            mLocationMassege = locationUtils.getMessage(mLocation);
        }

        mHandlerThread = new HandlerThread(getPackageName());
        mHandlerThread.start();
        mHandler = new MyHandle(new WeakReference<>(GpsService.this), mHandlerThread.getLooper());
        mHandler.sendEmptyMessageDelayed(GPS_DATA, 5 * 1000);
        mHandler.sendEmptyMessageDelayed(RECONNECT, 20 * 60 * 1000);


        return START_NOT_STICKY;
    }

    public static Criteria getLocationProvider() {
        Criteria mCriteria = new Criteria();
        mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        mCriteria.setAltitudeRequired(true);
        mCriteria.setBearingRequired(true);
        mCriteria.setCostAllowed(true);
        mCriteria.setSpeedRequired(true);
        mCriteria.setPowerRequirement(Criteria.POWER_LOW);
        return mCriteria;
    }

    private void getdates() {
        String ip = Settings.System.getString(getContentResolver(), IP);
        int port = Settings.System.getInt(getContentResolver(), PORT, 0);
        int callbalk_time = Settings.System.getInt(getContentResolver(), TIMES, 0);
        String id = Settings.System.getString(getContentResolver(), ID);
        if (ip != null && !ip.equals("")) {
            GPS_IP = ip;
        }
        if (port != 0) {
            GPS_PORT = port + "";
        }
        if (id != null && !id.equals("")) {
            PHONE_NUMBER = id;
        }
        if (callbalk_time != 0) {
            CALLBALK_TIME = callbalk_time * 1000;
        }
//         Toast.makeText(getApplicationContext(), "ip" + GPS_IP + "port" + GPS_PORT, Toast.LENGTH_LONG).show();
        Log.e("getdates", "ip" + GPS_IP + "   port" + GPS_PORT + "   id" + PHONE_NUMBER + "   callbalk_time" + CALLBALK_TIME);
    }

    private void startSocket(final String host, final String port) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                mPort = Integer.parseInt(port);
                try {
                    SocketAddress service = new InetSocketAddress(host, mPort);
                    mSocketLocation = new Socket();
                    mSocketLocation.connect(service, 15 * 1000);
                    mSocketLocation.setReuseAddress(true);
                    mSocketLocation.setKeepAlive(true);
                    mInputStream = mSocketLocation.getInputStream();
                    mOutputStream = mSocketLocation.getOutputStream();
                } catch (Exception e) {
                    e.printStackTrace();
                    reset();
                    try {
                        Thread.sleep(5 * 1000);
                        startSocket(host, port);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void sendCommand(byte[] cmd) {
        Log.e("zzx", "sendCommand()" + cmd.length);
        if (cmd == null) {
            return;
        }
        try {
            if (mOutputStream != null) {
                mOutputStream.write(cmd);
                mOutputStream.flush();
            } else {
                if (mSocketLocation != null) {
                    mOutputStream = mSocketLocation.getOutputStream();
                } else {
                    reset();
                    getdates();
                    startSocket(GPS_IP, PORT);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reset() {
        try {
            if (mSocketLocation != null) {
                mSocketLocation.close();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mOutputStream != null) {
                mOutputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取消息头
     */
    public static String getHeaders() {
        if (PHONE_NUMBER.length() < 12) {
            PHONE_NUMBER = "0" + PHONE_NUMBER;
        }
        return NEW_ID + NEW_PROPERTY + PHONE_NUMBER + NEW_SERIAL_NUMBER;
    }

    /**
     * 获取数据
     */
    public static String getDate() {
        if (mLocation != null) {
            mLocation = locationApplaction.getLoation();
            NEWS = locationUtils.getMessage(mLocation).toLowerCase();
            HEADERS = getHeaders();
            CHECK_CODE = locationUtils.getCheckCode(HEADERS + NEWS);
//            Toast.makeText(GpsService.mContext, SIGN + HEADERS + NEWS + CHECK_CODE + SIGN, Toast.LENGTH_SHORT).show();
//            Toast.makeText(GpsService.mContext, mLocation.toString(), Toast.LENGTH_LONG).show();
            Log.e("getdate", mLocation.toString());
            return SIGN + HEADERS + NEWS + CHECK_CODE + SIGN;
        } else {
            if (true) {
                mLocation = locationApplaction.getLoation();
                Log.e(TAG, mLocation.toString());
            } else {
                mLocation = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(getLocationProvider(), true));
                locationUtils.getMessage(mLocation);
                mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(getLocationProvider(), true), 1000, 1, mLocationListener);
            }

        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reset();
        unregisterReceiver(myReceiver);
    }

    private static LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: ");
            mLocationMassege = locationUtils.getMessage(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.e(TAG, "onProviderEnabled: ");
            locationUtils.getMessage(mLocationManager.getLastKnownLocation(s));
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.e(TAG, "onProviderDisabled: ");
            locationUtils.getMessage(null);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_GPSSERVICE)) {
                getdates();
                Log.e(TAG, "onStartCommand: " + "接收广播成功");
                if (mSocketLocation != null) {
                    try {
                        mSocketLocation.close();
                        Log.e(TAG, "onReceive: " + GPS_IP + "   " + GPS_PORT);
                        startSocket(GPS_IP, GPS_PORT);
                        isGetLocation = true;
                        mHandler.sendEmptyMessageDelayed(GPS_DATA, 1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    startSocket(GPS_IP, GPS_PORT);
                    isGetLocation = true;
                    mHandler.sendEmptyMessageDelayed(GPS_DATA, 1000);
                }
            }
        }
    }


    class MyHandle extends Handler {
        private WeakReference<GpsService> mReference;

        public MyHandle(WeakReference<GpsService> reference, Looper looper) {
            super(looper);
            mReference = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            GpsService service = mReference.get();
            if (service == null) {
                return;
            }
            switch (msg.what) {
                case GPS_DATA:
                    String dataMessage = getDate();
//                    Toast.makeText(service, "dataMessage"+dataMessage, Toast.LENGTH_SHORT).show();
                    if (dataMessage != null) {
                        if (mSocketLocation != null) {
                            sendCommand(ByteUtil.hexStringToBytes(dataMessage));
                        }
                        Log.e(TAG, ByteUtil.bytes2hex(ByteUtil.hexStringToBytes(dataMessage)));
//                        Toast.makeText(mContext, ByteUtil.bytes2hex(ByteUtil.hexStringToBytes(getDate())), Toast.LENGTH_SHORT).show();
                        mHandler.removeMessages(GPS_DATA);
                        if (isGetLocation) {
                            Toast.makeText(mContext, R.string.isgetlocation, Toast.LENGTH_SHORT).show();
                            isGetLocation = false;
                        }
                        mHandler.sendEmptyMessageDelayed(GPS_DATA, CALLBALK_TIME);
                    } else {
                        mHandler.removeMessages(GPS_DATA);
                        mHandler.sendEmptyMessageDelayed(GPS_DATA, 5000);
                    }

                    break;
                case RECONNECT:
                    if (mSocketLocation != null) {
                        try {
                            mSocketLocation.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        startSocket(GPS_IP, GPS_PORT);
                        mHandler.removeMessages(GPS_DATA);
                        mHandler.removeMessages(RECONNECT);
                        mHandler.sendEmptyMessageDelayed(GPS_DATA, 5000);
                        mHandler.sendEmptyMessageDelayed(RECONNECT, 20 * 60 * 1000);
                        break;
                    }
            }
        }
    }
}
