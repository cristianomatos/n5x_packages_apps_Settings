/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class QuietHours extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener  {

    private static final String TAG = "QuietHours";
    private static final String KEY_QUIET_HOURS_AUTO = "quiet_hours_auto";
    private static final String KEY_QUIET_HOURS_TIMERANGE = "quiet_hours_timerange";
    private static final CharSequence KEY_QUIET_HOURS_RINGER = "quiet_hours_ringer";

    private ListPreference mAutoEnable;
    private TimeRangePreference mQuietHoursTimeRange;
    private ListPreference mQuietHoursRinger;

    private SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.quiet_hours_settings);
        Resources res = getResources();
        ContentResolver resolver = getContentResolver();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        // Load the preferences
        mAutoEnable =
                (ListPreference) findPreference(KEY_QUIET_HOURS_AUTO);
        mQuietHoursTimeRange =
                (TimeRangePreference) findPreference(KEY_QUIET_HOURS_TIMERANGE);
        mQuietHoursTimeRange.setTimeRange(
                Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_START, 0),
                Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_END, 0));
        mQuietHoursTimeRange.setOnPreferenceChangeListener(this);

        mAutoEnable.setValue(mPrefs.getString(KEY_QUIET_HOURS_AUTO, "0"));
        mAutoEnable.setOnPreferenceChangeListener(this);

        // Remove the ringer setting on non-telephony devices else enable it
        mQuietHoursRinger = (ListPreference) findPreference(KEY_QUIET_HOURS_RINGER);
        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            getPreferenceScreen().removePreference(mAutoEnable);
            getPreferenceScreen().removePreference(mQuietHoursRinger);
            mQuietHoursRinger = null;
        } else {
            int autoMode = Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_AUTOMATIC, 0);
            int muteType = Settings.System.getInt(resolver, Settings.System.QUIET_HOURS_RINGER, 0);
            mQuietHoursRinger.setValue(String.valueOf(muteType));
            mQuietHoursRinger.setSummary(mQuietHoursRinger.getEntry());
            mQuietHoursRinger.setOnPreferenceChangeListener(this);
            mAutoEnable.setSummary(mAutoEnable.getEntries()[autoMode]);
        }

        // Remove the notification light setting if the device does not support it
        if (!res.getBoolean(com.android.internal.R.bool.config_intrusiveNotificationLed)) {
            removePreference(Settings.System.QUIET_HOURS_DIM);
        }

        // Remove the vibrator dependent settings if the device does not have a vibrator
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
               removePreference(Settings.System.QUIET_HOURS_STILL);
               removePreference(Settings.System.QUIET_HOURS_HAPTIC);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getContentResolver();
        if (preference == mAutoEnable) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_AUTOMATIC,
                    val);
            if (val != 0) {
                AudioManager audioManager =
                        (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                final int ringerMode = audioManager.getRingerMode();
                boolean enabled = false;
                if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                    enabled = true;
                } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                    if (val == 2) {
                        enabled = true;
                    }
                }
            }
            mAutoEnable.setSummary(mAutoEnable.getEntries()[val]);
            return true;
        } else if (preference == mQuietHoursTimeRange) {
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_START,
                    mQuietHoursTimeRange.getStartTime());
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_END,
                    mQuietHoursTimeRange.getEndTime());
            return true;
        } else if (preference == mQuietHoursRinger) {
            int ringerMuteType = Integer.valueOf((String) newValue);
            int index = mQuietHoursRinger.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.QUIET_HOURS_RINGER, ringerMuteType);
            mQuietHoursRinger.setSummary(mQuietHoursRinger.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mQuietHoursTimeRange != null) {
            mQuietHoursTimeRange.updatePreferenceViews();
        }
    }
}
