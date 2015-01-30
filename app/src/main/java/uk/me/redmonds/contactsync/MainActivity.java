package uk.me.redmonds.contactsync;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
//import android.widget.ArrayAdapter;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemSelectedListener;
//import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Spinner;
import android.accounts.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    public static final String TYPE = "com.google";
    public static final String ACCOUNT1 = "contact_syncAccount1";
    public static final String ACCOUNT2 = "contact_syncAccount2";
    //private SharedPreferences settings;
    private String account1Name;
    private String account2Name;
    private String syncType = "";
    public Menu mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        String trace = "";
        String line;
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(this.openFileInput("stack.trace")));
            while((line = reader.readLine()) != null) {
                trace += line+"\n";
            }
        } catch(FileNotFoundException fnfe) {
            return;
        } catch(IOException ioe) {
            return;
        }

        TopExceptionHandler.sendReport (this, trace);

        this.deleteFile("stack.trace");
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new SettingsFragment())
                        .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
            default:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_settings);
                break;
            case 2:
                mTitle = getString(R.string.title_sync);
                break;
            case 3:
                mTitle = getString(R.string.title_logs);
                break;
            case 4:
                mTitle = getString(R.string.title_results);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public void syncStatus (String type) {
        syncType = type;

        // rest stored results and sync matched to enable full sync to be run
        if (syncType == SyncFragment.FULL) {

        }
    }

    public void matchStatus (String type) {
        // get accounts to matched
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        //String account1Name = pref.getString(SettingsFragment.ACCOUNT1, null);
        //String account2Name = pref.getString(SettingsFragment.ACCOUNT2, null);
        SharedPreferences.Editor results = getPreferences(Context.MODE_PRIVATE).edit();
        results.putBoolean(Match.SYNCMATCHED, false);
        results.remove(Match.DUPKEY + account1Name);
        results.remove(Match.DUPKEY + account2Name);
        results.remove(Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name);
        results.remove(Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name);
        results.remove(Match.MATCHEDKEY + account1Name + ":" + account2Name);
        results.remove(Match.MATCHEDKEY + account2Name + ":" + account1Name);
        results.remove(Match.ACCOUNTKEY + account1Name + ":" + account2Name);
        results.remove(Match.ACCOUNTKEY + account2Name + ":" + account1Name);
        results.commit();

        // Create fragment and give it an argument specifying the article it should show
        StatusFragment newFragment = new StatusFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    public void onViewCreated(View statusView) {
        Match m = new Match();
        m.startMatch(this, statusView, syncType);
    }

    /*public void setMenuIcon () {
        //set icon dependent on visibility of settings screen
        FragmentManager fragMan = getSupportFragmentManager();
        SettingsFragment s = (SettingsFragment)fragMan.findFragmentByTag("com.redmonds.contactsync-settings");
        if (s != null && s.isAdded()) {
            mainMenu.findItem(R.id.settings).setIcon(CLOSE_ICON);
        } else {
            mainMenu.findItem(R.id.settings).setIcon(SETTINGS_ICON);
        }
    }*/

    public void hideMenuIcon () {
        if (mainMenu != null) {
            //mainMenu.findItem(R.id.settings).setVisible(false);
        }
    }

    public void showMenuIcon () {
        if (mainMenu != null) {
            //mainMenu.findItem(R.id.settings).setVisible(true);
        }
    }

    public void showResults () {
        showResults(null);
    }

    public void showResults (String item) {
        FragmentManager fragMan = getFragmentManager();
        FragmentTransaction transaction = fragMan.beginTransaction();
        SyncFragment newFragment = new SyncFragment();

        // Pass what list to show
        Bundle args = new Bundle();
        args.putString("list_type", SyncFragment.SUMMARY);
        if (item != null) {
            args.putString("list_item", item);
        }
        newFragment.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    public void Compare (String listType, String listItem, String selected) {
        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction transaction = fragMan.beginTransaction();
        CompareFragment newFragment = new CompareFragment();

        // Pass what list to show
        Bundle args = new Bundle();
        if (listType != null) {
            args.putString("listType", listType);
        }
        if (listItem != null) {
            args.putString("listItem", listItem);
        }
        if (selected != null) {
            args.putString("selected", selected);
        }
        newFragment.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        transaction.replace(R.id.fragment_container, newFragment, "com.redmonds.contactsync-compare");

        transaction.addToBackStack(null);

        transaction.commit();
    }

    public void Merge (String name, ArrayList<String> ids, String listItem) {
        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction transaction = fragMan.beginTransaction();
        MergeFragment newFragment = new MergeFragment();

        // Pass what list to show
        Bundle args = new Bundle();
        if (name != null) {
            args.putString("name", name);
        }
        if (ids != null) {
            args.putStringArrayList("ids", ids);
        }
        if (listItem != null) {
            args.putString("listItem", listItem);
        }

        SharedPreferences.Editor pref = getPreferences(Context.MODE_PRIVATE).edit();
        pref.remove("contactMerge");
        pref.remove("contactsMerge");
        pref.commit();

        newFragment.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        transaction.replace(R.id.fragment_container, newFragment, "uk.me.redmonds.contactsync-merge");

        //remove sub fragments
        for (Fragment f : getActiveFragments()) {
            if (f.getTag() == null)
                transaction.remove(f);
        }

        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onAttachFragment (Fragment fragment) {
        fragList.add(new WeakReference(fragment));
    }

    public ArrayList<Fragment> getActiveFragments() {
        ArrayList<Fragment> ret = new ArrayList<Fragment>();
        for(WeakReference<Fragment> ref : fragList) {
            Fragment f = ref.get();
            if(f != null && f.isVisible()) {
                ret.add(f);
            }
        }
        return ret;
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
