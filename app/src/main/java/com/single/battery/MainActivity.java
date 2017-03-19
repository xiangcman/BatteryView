package com.single.battery;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.library.battery.BatteryStatus;
import com.library.battery.BatteryView;

public class MainActivity extends AppCompatActivity {
    BatteryView batteryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        batteryView = (BatteryView) findViewById(R.id.batteryView);
        batteryView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                batteryView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(new BatteryReceiver(), filter);
            }
        });
    }

    public void setBattery(BatteryStatus status) {
        Log.d("TAG", "status:" + status.status + ",level:" + status.level);
        batteryView.setChanges(status.status, status.level);
    }
}
