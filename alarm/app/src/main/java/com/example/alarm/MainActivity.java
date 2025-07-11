package com.example.alarm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.alarmopensource.R;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView alarmTimeTextView;
    private TextView currentTimeText;


    public static Integer alarmIdentifier = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Check and request permission for exact alarms 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !getSystemService(AlarmManager.class).canScheduleExactAlarms()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }


        TimePicker timePicker = findViewById(R.id.timePicker);
        Button setAlarmButton = findViewById(R.id.setAlarmButton);
        alarmTimeTextView = findViewById(R.id.alarmTimeTextView);


        Calendar currentTime = Calendar.getInstance();
        currentTimeText = findViewById(R.id.currentTimeText);
        updateCurrentTimeTextView(currentTime);


        updateOldAlarmOnReopen();

        setAlarmButton.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());

             calendar.add(Calendar.SECOND, 15); // Adds 10 seconds to the current time

            if (calendar.after(now)) {
                setAlarm(calendar.getTimeInMillis());
                updateAlarmTextView(calendar);

                Log.d("MainActivity", "Alarm set for: " + formatTime(calendar));
            } else {
                Toast.makeText(MainActivity.this, "Cannot set alarm for past time.", Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Attempted to set an alarm for the past. Operation cancelled.");
            }
        });


        Button cancelAlarmButton = findViewById(R.id.cancelAlarmButton);
        cancelAlarmButton.setOnClickListener(v ->cancelAlarm());

    }


    private void setAlarm(long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmIdentifier, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);

        // Save alarm set time to SharedPreferences
        getSharedPreferences(getPackageName()+"AlarmOpenSourceApp", MODE_PRIVATE)
                .edit()
                .putLong("alarmTime" + alarmIdentifier, timeInMillis)
                .apply();
    }

    // Cancel Alarm by User
    private void cancelAlarm() {
        Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmIdentifier, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        // Clear saved alarm 
        getSharedPreferences(getPackageName() + "AlarmOpenSourceApp", MODE_PRIVATE)
                .edit()
                .remove("alarmTime" + alarmIdentifier)
                .apply();

        alarmTimeTextView.setText("Alarm not set");
    }
    private void updateOldAlarmOnReopen(){

        // Check if there is a saved alarm time and display it
        long savedAlarmTime = getSharedPreferences(getPackageName()+"AlarmOpenSourceApp", MODE_PRIVATE).getLong("alarmTime" + alarmIdentifier, 0);

        if (savedAlarmTime != 0) {
            // There is a saved alarm time, update the TextView
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(savedAlarmTime);
            updateAlarmTextView(alarmTime);
        }
    }

    private void updateCurrentTimeTextView(Calendar calendar){
        String alarmText = "Current Time: " + formatTime(calendar);
        currentTimeText.setText(alarmText);
    }

    private void updateAlarmTextView(Calendar calendar) {
        String alarmText = "Alarm set for: " + formatTime(calendar);
        alarmTimeTextView.setText(alarmText);
    }

    static String formatTime(Calendar calendar) {
        // Format the calendar time to a more readable form
        String timeFormat = "hh:mm:ss a"; // Example "03:00 PM"
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(timeFormat);
        return sdf.format(calendar.getTime());
    }

    // Just for tickering the time.
    private final Handler handler = new Handler();
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            // Update the current time text view
            Calendar currentTime = Calendar.getInstance();
            updateCurrentTimeTextView(currentTime);
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        ticker.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }
}
