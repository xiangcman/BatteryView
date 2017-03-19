package com.single.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.library.battery.BatteryStatus;
import com.library.battery.BatteryView;

public class BatteryReceiver extends BroadcastReceiver {
    private BatteryStatus status = new BatteryStatus();

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            status.status = intent.getIntExtra("status", 0);
            status.level = intent.getIntExtra("level", 0);
            switch (status.status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    status.status = BatteryView.STATUS_CHARGING;
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    status.status = BatteryView.STATUS_UNCHARGING;
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    status.status = BatteryView.STATUS_CHARGING;
                    break;
            }
            ((MainActivity) context).setBattery(status);
        }
    }


}
