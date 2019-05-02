package com.example.eventerapp.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.example.eventerapp.R;

import static android.content.Context.POWER_SERVICE;

public class SettingsFragment extends PreferenceFragmentCompat {

    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.settings_pref);

            sharedPreferences = getPreferenceScreen().getSharedPreferences();

        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Intent intent = new Intent();
                PowerManager powerManager = (PowerManager) getContext().getSystemService(POWER_SERVICE);
                if (sharedPreferences.getBoolean(key, false)) {
                    if (powerManager.isIgnoringBatteryOptimizations(getContext().getPackageName())) {
                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    } else {
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                    }
                    startActivity(intent);
                } else {
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                }
            }
        };

        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }
}
