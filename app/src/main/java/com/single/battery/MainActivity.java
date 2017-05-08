package com.single.battery;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.library.battery.BatteryStatus;
import com.library.battery.BatteryView;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    BatteryView batteryView;

    Timer timer;

    int level;

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
                //模拟的代码
//                timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                level++;
//                                if (level == 100) {
//                                    level = 0;
//                                }
//                                batteryView.setChanges(BatteryView.STATUS_CHARGING, level);
//                            }
//                        });
//                    }
//                }, 0, 500);
            }
        });
    }

    public void setBattery(BatteryStatus status) {
        Log.d("TAG", "status:" + status.status + ",level:" + status.level);
        batteryView.setChanges(status.status, status.level);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        timer.cancel();
    }
}
