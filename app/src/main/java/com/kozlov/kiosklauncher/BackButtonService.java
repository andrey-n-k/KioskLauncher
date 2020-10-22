package com.kozlov.kiosklauncher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class BackButtonService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private static final int LayoutParamFlags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

    private View mLayoutView;
    private WindowManager mWindowManager;

    private BroadcastReceiver mBr;

    @Override
    public void onCreate() {
        super.onCreate();

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                LayoutParamFlags,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(this);
        mLayoutView = inflater.inflate(R.layout.back_button, null);
        mLayoutView.setVisibility(View.GONE);
        mWindowManager.addView(mLayoutView, params);


        mLayoutView.findViewById(R.id.home_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity((new Intent(Intent.ACTION_MAIN)).addCategory(Intent.CATEGORY_HOME));
            }
        });

        mBr = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == "com.kozlov.kiosklauncher.ACTION_SHOW_HOME_BUTTON")
                {
                    Log.d("KIOSK", "SHOW HOME");
                    mLayoutView.setVisibility(View.VISIBLE);
                }
                else if (intent.getAction() == "com.kozlov.kiosklauncher.ACTION_HIDE_HOME_BUTTON")
                {
                    Log.d("KIOSK", "HIDE HOME");
                    mLayoutView.setVisibility(View.GONE);
                }
            }
        };

        IntentFilter intFilt = new IntentFilter();
        intFilt.addAction("com.kozlov.kiosklauncher.ACTION_SHOW_HOME_BUTTON");
        intFilt.addAction("com.kozlov.kiosklauncher.ACTION_HIDE_HOME_BUTTON");
        registerReceiver(mBr, intFilt);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mLayoutView);
        unregisterReceiver(mBr);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
