/*
 * Copyright (C) 2014 Slimroms
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

package com.android.settings.n5x;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Gravity;

import com.android.internal.util.n5x.DeviceUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.n5x.util.Helpers;

public class HoverSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "HoverSettings";

    private static final String PREF_HOVER_LONG_FADE_OUT_DELAY = "hover_long_fade_out_delay";

    ListPreference mHoverLongFadeOutDelay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.hover_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mHoverLongFadeOutDelay = (ListPreference) prefSet.findPreference(PREF_HOVER_LONG_FADE_OUT_DELAY);
        int HoverLongFadeOutDelay = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.HOVER_LONG_FADE_OUT_DELAY, 5000, UserHandle.USER_CURRENT);
        mHoverLongFadeOutDelay.setValue(String.valueOf(HoverLongFadeOutDelay));
        mHoverLongFadeOutDelay.setSummary(mHoverLongFadeOutDelay.getEntry());
        mHoverLongFadeOutDelay.setOnPreferenceChangeListener(this);

        //

        UpdateSettings();
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateSettings();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void UpdateSettings() {
        //
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHoverLongFadeOutDelay) {
            int index = mHoverLongFadeOutDelay.findIndexOfValue((String) newValue);
            int HoverLongFadeOutDelay = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                Settings.System.HOVER_LONG_FADE_OUT_DELAY,
                    HoverLongFadeOutDelay, UserHandle.USER_CURRENT);
            mHoverLongFadeOutDelay.setSummary(mHoverLongFadeOutDelay.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
