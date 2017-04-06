package com.stc.smartbulb.trigger;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.stc.smartbulb.R;
import com.stc.smartbulb.model.NetworkChangeReceiver;
import com.stc.smartbulb.utils.PrefsUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TriggerActivity extends AppCompatActivity {
    private static final String TAG = "TriggerActivity";
    TextView textWifi, textTimeOn, textTimeOff;
    Button btnWifi;
    CheckBox checkboxEnabled;
    private AlertDialog dialogTimePicker, dialogWifiSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        textWifi=(TextView) findViewById(R.id.text_ssid);
        textTimeOff=(TextView) findViewById(R.id.text_time_off);
        textTimeOn=(TextView) findViewById(R.id.text_time_on);
        btnWifi=(Button) findViewById(R.id.button_wifi);
        checkboxEnabled =(CheckBox) findViewById(R.id.checkBox);
        initUi();

    }
    private void initUi(){
        String ssid = PrefsUtils.getSavedWifiSsid(this);
        long timeOn = PrefsUtils.getSavedTimeOn(this);
        long timeOff = PrefsUtils.getSavedTimeOff(this);
        boolean triggerEnabled=PrefsUtils.getSavedTrggerWifiEnabled(this);
        Log.d(TAG, "initUi: ssid="+ssid);
        Log.d(TAG, "initUi: timeOn="+timeOn);
        Log.d(TAG, "initUi: timeOff="+timeOff);
        Log.d(TAG, "initUi: triggerEnabled="+triggerEnabled);
        checkboxEnabled.setEnabled(triggerEnabled);
        if(ssid!=null && ssid.length()>0) {
            textWifi.setText(ssid);
            checkboxEnabled.setEnabled(true);
        }else {
            textWifi.setText("");
            checkboxEnabled.setEnabled(false);
        }
        if(timeOn!=Long.MIN_VALUE) {
            String timeOnString = new SimpleDateFormat("HH:mm").format(new Date(timeOn));
            textTimeOn.setText(timeOnString);
        }else {
            textTimeOn.setText("");
        }
        if(timeOff!=Long.MIN_VALUE) {
            String timeOffString = new SimpleDateFormat("HH:mm").format(new Date(timeOff));
            textTimeOff.setText(timeOffString);
        }else {
            textTimeOff.setText("");
        }
        btnWifi.setOnClickListener(v -> showWifiSelectDialog());
        textTimeOn.setOnClickListener(v -> showTimePickerDialog(v.getId()));
        textTimeOff.setOnClickListener(v -> showTimePickerDialog(v.getId()));
        checkboxEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PrefsUtils.saveTriggerEnabled(TriggerActivity.this, isChecked);
        });
    }

    private void showWifiSelectDialog() {
        Log.d(TAG, "showWifiSelectDialog: ");
        String ssid = NetworkChangeReceiver.getConnectedWifiSsid(TriggerActivity.this);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TriggerActivity.this)
                .setTitle(R.string.title_set_wifi)
                .setCancelable(true)
                .setNegativeButton("cancel", (dialog, which) -> {
                    dialog.cancel();
                });
        if(ssid!=null) {
            dialogBuilder.setPositiveButton("select", (dialog, which) -> {
                onWifiSelected(ssid);
                dialog.cancel();
            }).setMessage(ssid);
        }else {
            dialogBuilder.setMessage(R.string.wifi_not_connected);
        }
        dialogWifiSelect=dialogBuilder.create();
        dialogWifiSelect.show();
    }

    private void showTimePickerDialog(int whichTime){
        // TODO: 4/6/17 clockformat
        Log.d(TAG, "showTimePickerDialog: "+whichTime);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TriggerActivity.this)
                .setView(R.layout.dialog_time_picker)
                .setCancelable(true)
                .setNegativeButton("cancel", (dialog, which) -> {
                    dialog.cancel();
                });
            dialogBuilder.setPositiveButton("select", (dialog, which) -> {
                TimePicker timePicker = (TimePicker) dialogTimePicker.findViewById(R.id.time_picker);
                int hour=timePicker.getHour();
                int minute = timePicker.getMinute();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                long timeSelected = calendar.getTimeInMillis();
                onTimeSelected(whichTime, timeSelected);
                dialog.cancel();
            });
        dialogTimePicker=dialogBuilder.create();
        dialogTimePicker.show();
    }
    private void onWifiSelected(String wifi){
        if(wifi==null) {
            Toast.makeText(this, "error no wifi", Toast.LENGTH_SHORT).show();
            return;
        }
        textWifi.setText(wifi);
        PrefsUtils.saveWifiSSID(this, wifi);
        checkboxEnabled.setEnabled(true);
    }
    private void onTimeSelected(int which , long val){
        String timeString = new SimpleDateFormat("HH:mm").format(new Date(val));
        Log.d(TAG, "onTimeSelected: "+which+"val="+timeString);
        if(which==textTimeOn.getId()){
            textTimeOn.setText(timeString);
            PrefsUtils.saveTimeOn(this, val);
        }else if(which==textTimeOff.getId()){
            textTimeOff.setText(timeString);
            PrefsUtils.saveTimeOff(this, val);
        }
    }

    private void scheduleJob(String cmd, long when){
        TriggerScheduler.scheduleAlarm(this,when,cmd);
    }


}
