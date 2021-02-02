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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.android.internal.logging.nano.MetricsProto;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class SmartMedia extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String SMART_MEDIA_TITLE_FONT = "smart_media_title_font";
    private static final String SMART_MEDIA_ARTIST_FONT = "smart_media_artist_font";

    private ListPreference mSmartMediaTitleFonts;
    private ListPreference mSmartMediaArtistFonts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.smart_media_settings);

        mSmartMediaTitleFonts = (ListPreference) findPreference(SMART_MEDIA_TITLE_FONT);
        mSmartMediaTitleFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.SMART_MEDIA_TITLE_FONT, 0)));
        mSmartMediaTitleFonts.setSummary(mSmartMediaTitleFonts.getEntry());
        mSmartMediaTitleFonts.setOnPreferenceChangeListener(this);

        // Lockscren Date Fonts
        mSmartMediaArtistFonts = (ListPreference) findPreference(SMART_MEDIA_ARTIST_FONT);
        mSmartMediaArtistFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.SMART_MEDIA_ARTIST_FONT, 0)));
        mSmartMediaArtistFonts.setSummary(mSmartMediaArtistFonts.getEntry());
        mSmartMediaArtistFonts.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mSmartMediaTitleFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.SMART_MEDIA_TITLE_FONT,
                    Integer.valueOf((String) newValue));
            mSmartMediaTitleFonts.setValue(String.valueOf(newValue));
            mSmartMediaTitleFonts.setSummary(mSmartMediaTitleFonts.getEntry());
            return true;
        } else if (preference == mSmartMediaArtistFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.SMART_MEDIA_ARTIST_FONT,
                    Integer.valueOf((String) newValue));
            mSmartMediaArtistFonts.setValue(String.valueOf(newValue));
            mSmartMediaArtistFonts.setSummary(mSmartMediaArtistFonts.getEntry());
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
                sir.xmlResId = R.xml.smart_media_settings;
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
