package uk.me.redmonds.contactsync;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.fragment_settings);

        //get the account saved variable
        ListPreference account1Pref = (ListPreference) findPreference(MainActivity.ACCOUNT1);
        ListPreference account2Pref = (ListPreference) findPreference(MainActivity.ACCOUNT2);

        // Create an ArrayAdapter using the string array and a default spinner layout
        AccountManager accounts = AccountManager.get(this.getActivity());

        Account accountsList[] = accounts.getAccountsByType(getString(R.string.type));
        ArrayList<CharSequence> list = new ArrayList<>();

        for (Account a : accountsList) {
            list.add(a.name);
        }

        CharSequence[] listArray = list.toArray(new CharSequence[list.size()]);

        account1Pref.setEntries(listArray);
        account1Pref.setEntryValues(listArray);

        account2Pref.setEntries(listArray);
        account2Pref.setEntryValues(listArray);

        //set saved values to summary
        ListPreference listPref = (ListPreference) account1Pref;
        account1Pref.setSummary(listPref.getEntry());
        listPref = (ListPreference) account2Pref;
        account2Pref.setSummary(listPref.getEntry());
    }

    @Override
    public void onResume() {
        super.onResume();
        
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        //set actionbar title
        ((MainActivity)getActivity()).setHeading(getString(R.string.title_settings));
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }
}
