package com.test.getlocation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView tv1, tv2, tvPlace;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv1 = findViewById(R.id.tv_1);
        tv2 = findViewById(R.id.tv_2);
        tvPlace = findViewById(R.id.tv_place);
        findViewById(R.id.tv_x).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGPS();
                Toast.makeText(MainActivity.this, "点击", Toast.LENGTH_LONG).show();
            }
        });
        requestPermission();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Toast.makeText(MainActivity.this, "已开启定位权限", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    public void getGPS() {
        // 获取位置管理服务
        LocationManager locationManager;
        String serviceName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) this.getSystemService(serviceName);
        // 查找到服务信息
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

        String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
        Location location = null;
        locationListener = getListener();

        if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // 通过NETWORK获取位置
            showLocation(location);
        } else if (provider.equals(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 通过GPS获取位置
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            if (location == null) {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // 通过NETWORK获取位置
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                }
            }
            showLocation(location);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "NO SERVICE ENABLED", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private LocationListener getListener() {
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                showLocation(location);
            }

            public void onProviderDisabled(String provider) {
                // Provider被disable时触发此函数，比如GPS被关闭
            }

            public void onProviderEnabled(String provider) {
                //  Provider被enable时触发此函数，比如GPS被打开
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
            }
        };
        return locationListener;
    }

    private void showLocation(Location location) {
        if (location == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "LOCATION IS NULL EXCEPTION", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            tv1.setText(String.valueOf(location.getLatitude()));    //经度
            tv2.setText(String.valueOf(location.getLongitude()));    //纬度
            showLocationAddress(location);
        }
    }

    private String showLocationAddress(Location location) {
        String add = "";
        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.CHINESE);
        try {
            List<Address> addresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address address = addresses.get(0);
            int maxLine = address.getMaxAddressLineIndex();
            if (maxLine >= 2) {
                add = address.getAddressLine(0) + address.getAddressLine(1);
            } else {
                add = address.getAddressLine(0);
            }
            tvPlace.setText(add);
        } catch (IOException e) {
            add = "";
            e.printStackTrace();
        }
        return add;
    }
}