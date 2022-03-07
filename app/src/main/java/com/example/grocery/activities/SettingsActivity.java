package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.Constans;
import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private SwitchCompat fcmSwitch;
    private TextView notificationStatusTv;

    public static final String enabledMessage = "Notifications are Enable ";
    public static final String disabledMessage = "Notifications are Disabled ";

    private boolean isChecked = false;

    private FirebaseAuth firebaseAuth;

    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        backBtn = findViewById(R.id.backBtn);
        fcmSwitch=findViewById(R.id.fcmSwitch);
        notificationStatusTv = findViewById(R.id.notificationStatusTv);

        sp = getSharedPreferences("SETTINGS_SP",MODE_PRIVATE);
        //check last selected option; true/false
        isChecked = sp.getBoolean("FCM_ENABLED",false);
        fcmSwitch.setChecked(isChecked);
        if (isChecked) {
            notificationStatusTv.setText(enabledMessage);
        } else {
            notificationStatusTv.setText(disabledMessage);
        }


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    subscribeToTopic();
                } else {
                    UnsubscribeToTopic();
                }
            }
        });

    }

    public void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constans.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //save setting
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",true);
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+enabledMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(enabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void UnsubscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constans.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",false);
                        spEditor.apply();

                        Toast.makeText(SettingsActivity.this, ""+disabledMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(disabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}