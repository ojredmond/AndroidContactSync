package uk.me.redmonds.contactsync;

import android.preference.PreferenceFragment;
import android.os.Bundle;

public static class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.layout.settings_fragment);
    }
}
