package com.example.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class AlarmBroadcastReceiver extends BroadcastReceiver {


        // call if any BroadcastReceiver message is received.
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("AlarmBroadcastReceiver", "Alarm is triggered!");
            Intent alarmIntent = new Intent(context, AlarmAlertActivity.class);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(alarmIntent);

        }
}
