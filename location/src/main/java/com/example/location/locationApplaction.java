package com.example.location;

import android.app.Application;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import static com.baidu.location.LocationClientOption.LOC_SENSITIVITY_HIGHT;

/**
 * Created by yyd on 2017/2/28.
 */
public class locationApplaction extends Application {

    /**
     * 这里坐标用的bd09ll，配置参数见 {@link #getDefaultLocationClientOption()}
     * 具体说明访问 http://lbsyun.baidu.com/index.php?title=android-locsdk/qa
     */


    public LocationClient mLocationClient = null;
    private static Location location = null;
    private LocationClientOption mOption;
    private MyLocationListener myListener = new MyLocationListener();
    Date d1;

    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
    //原有BDLocationListener接口暂时同步保留。具体介绍请参考后文中的说明
    public void onCreate() {
        super.onCreate();
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        //注册监听函数
        getDefaultLocationClientOption();

//        SDKInitializer.initialize(getApplicationContext());
        mLocationClient.start();

    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            PositionUtil.Gps gps = PositionUtil.gcj_To_Gps84(bdLocation.getLatitude(), bdLocation.getLongitude());
            String coorType = bdLocation.getCoorType();
            if (location == null) {
                location = new Location(bdLocation.getLocType() == BDLocation.TypeGpsLocation ? "gps定位" : "其他");
            }
            location.setLatitude(gps.getWgLat());//获取纬度信息
            location.setLongitude(gps.getWgLon());//获取经度信息
            location.setAltitude(bdLocation.getAltitude());
            location.setBearing(bdLocation.getDirection());
            location.setSpeed(bdLocation.hasSpeed() ? bdLocation.getSpeed() : 0);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String datestring = bdLocation.getTime();
            try {
                d1 = df.parse(datestring);
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            location.setTime(d1.getTime());
            location.setTime(System.currentTimeMillis());
//            location.setTime(Long.valueOf(getGpsLoaalTime(Long.valueOf(bdLocation.getTime()))));
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            //double latitude = bdLocation.getLatitude();    //获取纬度信息
            //double longitude = bdLocation.getLongitude();    //获取经度信息
            //float radius = bdLocation.getRadius();    //获取定位精度，默认值为0.0f


            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = bdLocation.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
//            Log.e("onReceiveLocation: ", latitude+"   "+longitude+"    "+radius+"   "+coorType+"   "+errorCode);
//            Log.e( "onReceiveLocation: ", location.getLatitude() + "   " + location.getLongitude() +
//                    "    " + location.getBearing() + "    " + location.getAltitude() + "   " + location.getTime() +
//                    "    " + location.getSpeed() + "   " + coorType + "   " + errorCode + "   " + bdLocation.getTime());
//            Toast.makeText(locationApplaction.this, location.getLatitude() + "   " + location.getLongitude() +
//                    "    " + location.getBearing() + "    " + location.getAltitude() + "   " + location.getTime() +
//                    "    " + location.getSpeed() + "   " + coorType + "   " + errorCode + "   " + bdLocation.getTime(), Toast.LENGTH_SHORT).show();
        }

    }

    public LocationClientOption getDefaultLocationClientOption() {
        if (mOption == null) {
            mOption = new LocationClientOption();
            mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            //可选，设置定位模式，默认高精度
            //LocationMode.Hight_Accuracy：高精度；
            //LocationMode. Battery_Saving：低功耗；
            //LocationMode. Device_Sensors：仅使用设备；

            mOption.setCoorType("gcj02");
            //可选，设置返回经纬度坐标类型，默认gcj02
            //gcj02：国测局坐标；
            //bd09ll：百度经纬度坐标；
            //bd09：百度墨卡托坐标；
            //海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标

            mOption.setScanSpan(1000);
            //可选，设置发起定位请求的间隔，int类型，单位ms
            //如果设置为0，则代表单次定位，即仅定位一次，默认为0
            //如果设置非0，需设置1000ms以上才有效

            mOption.setOpenGps(true);
            //可选，设置是否使用gps，默认false
            //使用高精度和仅用设备两种定位模式的，参数必须设置为true

            mOption.setLocationNotify(false);
            //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

            mOption.setIgnoreKillProcess(true);
            //可选，定位SDK内部是一个service，并放到了独立进程。
            //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

            mOption.SetIgnoreCacheException(false);
            //可选，设置是否收集Crash信息，默认收集，即参数为false

            mOption.setWifiCacheTimeOut(5 * 60 * 1000);
            //可选，7.2版本新增能力
            //如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位
            mOption.setEnableSimulateGps(false);
            //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
            mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
            mOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
            mOption.setIsNeedAltitude(true);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
            mLocationClient.setLocOption(mOption);
            //mLocationClient为第二步初始化过的LocationClient对象
            //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
            //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明

        }
        return mOption;
    }

    public static Location getLoation() {
        return location;
    }
}
