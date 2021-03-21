/*
 * Copyright (C) 2020 DerpFest
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

package com.derpquest.settings.fragments.lockscreen;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.ServiceManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.derp.support.preference.CustomSeekBarPreference;
import com.derp.support.colorpicker.ColorPickerPreference;

import com.android.internal.logging.nano.MetricsProto;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class LockscreenGeneral extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String AOD_SCHEDULE_KEY = "always_on_display_schedule";
    private static final String LOCK_CLOCK_FONT_STYLE = "lock_clock_font_style";
    private static final String LOCK_DATE_FONTS = "lock_date_fonts";
    private static final String FOD_ANIMATIONS = "fod_animations";
    private static final String AMBIENT_ICONS_COLOR = "ambient_icons_color";
    private static final String LOCK_ICON_POSITION = "lock_icon_position";
    private static final String LOCK_CLOCK_POSITION = "lock_clock_position";
    private static final String LOCK_OWNER_INFO_POSITION = "lock_owner_info_position";

    static final int MODE_DISABLED = 0;
    static final int MODE_NIGHT = 1;
    static final int MODE_TIME = 2;
    static final int MODE_MIXED_SUNSET = 3;
    static final int MODE_MIXED_SUNRISE = 4;

    private ListPreference mLockClockFonts;
    private ListPreference mLockDateFonts;
    private PreferenceCategory mFODCategory;
    private ColorPickerPreference mAmbientIconsColor;
    private ListPreference mLockIconPosition;
    private ListPreference mLockClockPosition;
    private ListPreference mLockOwnerInfoPosition;

    Preference mAODPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_general);
        PreferenceScreen prefSet = getPreferenceScreen();
        Context mContext = getContext();

        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONT_STYLE);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_CLOCK_FONT_STYLE, 0)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);

        // Lockscren Date Fonts
        mLockDateFonts = (ListPreference) findPreference(LOCK_DATE_FONTS);
        mLockDateFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_DATE_FONTS, 1)));
        mLockDateFonts.setSummary(mLockDateFonts.getEntry());
        mLockDateFonts.setOnPreferenceChangeListener(this);
        
        Resources res = mContext.getResources();
        boolean hasFod = res.getBoolean(com.android.internal.R.bool.config_needCustomFODView);

        mFODCategory = (PreferenceCategory) findPreference(FOD_ANIMATIONS);
        if (mFODCategory != null && !hasFod) {
            prefSet.removePreference(mFODCategory);
        }
        
        // Ambient Icons Color
        mAmbientIconsColor = (ColorPickerPreference) findPreference(AMBIENT_ICONS_COLOR);
        int intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.AMBIENT_ICONS_COLOR, Color.WHITE);
        String hexColor = String.format("#%08x", (0xffffff & intColor));
        mAmbientIconsColor.setNewPreviewColor(intColor);
        mAmbientIconsColor.setSummary(hexColor);
        mAmbientIconsColor.setOnPreferenceChangeListener(this);

        // Lock Icon Position
        mLockIconPosition = (ListPreference) findPreference(LOCK_ICON_POSITION);
        mLockIconPosition.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_ICON_POSITION, 1)));
        mLockIconPosition.setSummary(mLockIconPosition.getEntry());
        mLockIconPosition.setOnPreferenceChangeListener(this);

        // Lock Clock Position
        mLockClockPosition = (ListPreference) findPreference(LOCK_CLOCK_POSITION);
        mLockClockPosition.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_CLOCK_POSITION, 1)));
        mLockClockPosition.setSummary(mLockClockPosition.getEntry());
        mLockClockPosition.setOnPreferenceChangeListener(this);

        // Lock Owner Info Position
        mLockOwnerInfoPosition = (ListPreference) findPreference(LOCK_OWNER_INFO_POSITION);
        mLockOwnerInfoPosition.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_OWNER_INFO_POSITION, 1)));
        mLockOwnerInfoPosition.setSummary(mLockIconPosition.getEntry());
        mLockOwnerInfoPosition.setOnPreferenceChangeListener(this);

        mAODPref = findPreference(AOD_SCHEDULE_KEY);
        updateAlwaysOnSummary();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAlwaysOnSummary();
    }

    private void updateAlwaysOnSummary() {
        if (mAODPref == null) return;
        int mode = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.DOZE_ALWAYS_ON_AUTO_MODE, 0, UserHandle.USER_CURRENT);
        switch (mode) {
            default:
            case MODE_DISABLED:
                mAODPref.setSummary(R.string.disabled);
                break;
            case MODE_NIGHT:
                mAODPref.setSummary(R.string.night_display_auto_mode_twilight);
                break;
            case MODE_TIME:
                mAODPref.setSummary(R.string.night_display_auto_mode_custom);
                break;
            case MODE_MIXED_SUNSET:
                mAODPref.setSummary(R.string.always_on_display_schedule_mixed_sunset);
                break;
            case MODE_MIXED_SUNRISE:
                mAODPref.setSummary(R.string.always_on_display_schedule_mixed_sunrise);
                break;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockClockFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_CLOCK_FONT_STYLE,
                    Integer.valueOf((String) newValue));
            mLockClockFonts.setValue(String.valueOf(newValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
        } else if (preference == mLockDateFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_DATE_FONTS,
                    Integer.valueOf((String) newValue));
            mLockDateFonts.setValue(String.valueOf(newValue));
            mLockDateFonts.setSummary(mLockDateFonts.getEntry());
            return true;
        } else if (preference == mAmbientIconsColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                .parseInt(String.valueOf(newValue)));
            mAmbientIconsColor.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.AMBIENT_ICONS_COLOR, intHex);
        } else if (preference == mLockIconPosition) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_ICON_POSITION,
                    Integer.valueOf((String) newValue));
            mLockIconPosition.setValue(String.valueOf(newValue));
            mLockIconPosition.setSummary(mLockIconPosition.getEntry());
            return true;
        } else if (preference == mLockClockPosition) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_CLOCK_POSITION,
                    Integer.valueOf((String) newValue));
            mLockClockPosition.setValue(String.valueOf(newValue));
            mLockClockPosition.setSummary(mLockClockPosition.getEntry());
            return true;
        } else if (preference == mLockOwnerInfoPosition) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_OWNER_INFO_POSITION,
                    Integer.valueOf((String) newValue));
            mLockOwnerInfoPosition.setValue(String.valueOf(newValue));
            mLockOwnerInfoPosition.setSummary(mLockOwnerInfoPosition.getEntry());
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DERP;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                    boolean enabled) {
                final ArrayList<SearchIndexableResource> result = new ArrayList<>();
                final SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.lockscreen_general;
                result.add(sir);
                return result;
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                final List<String> keys = super.getNonIndexableKeys(context);
                return keys;
            }
    };
}
