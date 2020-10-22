package com.kozlov.kiosklauncher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = ((WebView) findViewById(R.id.webview));
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        findViewById(R.id.button_open_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        findViewById(R.id.button_wifi_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        findViewById(R.id.button_bluetooth_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        findViewById(R.id.button_start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(MainActivity.this, BackButtonService.class);
                serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

                ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
            }
        });

        findViewById(R.id.button_stop_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(MainActivity.this, BackButtonService.class);
                stopService(serviceIntent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (Settings.canDrawOverlays(this)) {
                Intent serviceIntent = new Intent(MainActivity.this, BackButtonService.class);
                serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

                ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
            } else {

            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sendBroadcast(new Intent().setAction("com.kozlov.kiosklauncher.ACTION_SHOW_HOME_BUTTON"));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mWebView.loadUrl("https://ruwelt.roboclimate.ru/?vId=78");
        sendBroadcast(new Intent().setAction("com.kozlov.kiosklauncher.ACTION_HIDE_HOME_BUTTON"));
        if(!Settings.canDrawOverlays(this)){
            // ask for setting
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 123);
        }
        else
        {
            if (!isMyAppLauncherDefault())
            {
                resetPreferredLauncherAndOpenChooser(this);
            }
            else
            {
                Intent serviceIntent = new Intent(MainActivity.this, BackButtonService.class);
                serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

                ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
            }
        }
    }

    private boolean isMyAppLauncherDefault()
    {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = (PackageManager) getPackageManager();

        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static void resetPreferredLauncherAndOpenChooser(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, FakeLauncherActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(selector);

        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }
}