package com.enterprise.charky.wisor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.enterprise.charky.wisor.R;

/**
 * Created by charky on 14.11.15.
 * Simple Settings
 */
public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }


    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences
            .OnSharedPreferenceChangeListener {

        private static final String[] STRING_PREFS = {
                "pref_key_server_name",
                "pref_key_server_port",
                "pref_key_ws1_name",
                "pref_key_ws2_name",
                "pref_key_ws3_name"
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            //Init Summaries
            setSummaries();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, ""));
        }

        private void setSummaries() {
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            for (String sPref : STRING_PREFS) {
                Preference pref = findPreference(sPref);
                pref.setSummary(sharedPreferences.getString(sPref, ""));
            }
        }
    }
}

