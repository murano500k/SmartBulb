/*
package com.stc.smartbulb.trigger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

*/
/**
 * Created by artem on 4/6/17.
 *//*


public class TriggerScheduler {
    private static final String TAG = "TriggerScheduler";
    public static void scheduleAlarm(Context context, long alarmTime, String action) {
        Log.d(TAG, "scheduleAlarm: ");
        //Schedule the alarm. Will update an existing item for the same task.
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent operation =
                TriggerAlarmService.getPendingIntent(context, action);

        manager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, operation);
    }
}
*/
