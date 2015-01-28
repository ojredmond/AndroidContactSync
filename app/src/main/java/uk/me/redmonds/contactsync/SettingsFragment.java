package uk.me.redmonds.contactsync;

import android.preference.PreferenceFragment;
import android.preference.PreferenceCategory;

public static class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
