package uk.me.redmonds.contactsync;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity
        implements StatusFragment.OnViewCreatedListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle = "";

    public static final String TYPE = "com.google";
    public static final String ACCOUNT1 = "account1";
    public static final String ACCOUNT2 = "account2";
    public static final String STATE_SELECTED_FRAGMENT = "selected_fragment";
    public static String PACKAGE_NAME;
    
    //private SharedPreferences settings;
    private String account1Name;
    private String account2Name;
    private String syncType = "";
    private Boolean mFromSavedInstanceState = false;
    public Menu mainMenu;
    private List<WeakReference<Fragment>> fragList = new ArrayList<WeakReference<Fragment>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getApplicationContext().getPackageName();
	
        if (savedInstanceState != null) {
            String mCurrentFragment = savedInstanceState.getString(STATE_SELECTED_FRAGMENT);
            Toast.makeText(context, mCurrentFragment, Toast.LENGTH_LONG).show();
            mFromSavedInstanceState = true;
        }

        ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.app_name);
		mTitle = getString(R.string.app_name);
		
		Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
		
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(actionBar,
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SELECTED_FRAGMENT, "Test");
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if(mFromSavedInstanceState)
            return;
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new SettingsFragment(), PACKAGE_NAME + "-" + getString(R.string.title_settings))
                        .addToBackStack(PACKAGE_NAME + "-" + getString(R.string.title_settings))
                        .commit();
                break;
            case 1:
                Fragment syncF = new SyncFragment();

                // Pass what list to show
                Bundle args = new Bundle();
                args.putString("list_type", SyncFragment.OPTIONS);
                syncF.setArguments(args);
                fragmentManager.beginTransaction()
                    .replace(R.id.container, syncF, PACKAGE_NAME + "-" + getString(R.string.title_sync))
                    .addToBackStack(PACKAGE_NAME + "-" + getString(R.string.title_sync))
                    .commit();
                break;
            case 2:
            	Fragment statusF = fragmentManager.findFragmentByTag(PACKAGE_NAME + "-" + getString(R.string.title_logs));
            	if (statusF == null) statusF = new StatusFragment();

                fragmentManager.beginTransaction()
                    .replace(R.id.container, statusF, PACKAGE_NAME + "-" + getString(R.string.title_logs))
                    .commit();
                break;
            case 3:
                showResults();
                break;
        }
    }

    public void setHeading(CharSequence title) {
        if(!title.equals("")) mTitle = title;
		
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);
		
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        setHeading(mTitle);
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
        String account1Name = pref.getString(ACCOUNT1, null);
        String account2Name = pref.getString(ACCOUNT2, null);
        SharedPreferences.Editor results = getPreferences(Context.MODE_PRIVATE).edit();
        results.putBoolean(Match.SYNCMATCHED, false);
        results.remove(Match.NUMCONTACTS + account1Name);
        results.remove(Match.NUMCONTACTS + account2Name);
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

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.container, newFragment, PACKAGE_NAME + "-" + getString(R.string.title_logs));
        transaction.addToBackStack(PACKAGE_NAME + "-" + getString(R.string.title_logs));

        // Commit the transaction
        transaction.commit();
    }

    public void onViewCreated(View statusView) {
        Match m = new Match();
        m.startMatch(this, statusView, syncType);
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
        transaction.replace(R.id.container, newFragment, PACKAGE_NAME + "-" + getString(R.string.title_results));
        transaction.addToBackStack(PACKAGE_NAME + "-" + getString(R.string.title_results));

        transaction.commit();
        
        //update the navigation drawer
		if(mNavigationDrawerFragment != null)
        	mNavigationDrawerFragment.changeItem(3);
    }

    public void Compare (String listType, String listItem, String selected) {
        FragmentManager fragMan = getFragmentManager();
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
        transaction.replace(R.id.container, newFragment, PACKAGE_NAME + "-compare");

        transaction.addToBackStack(PACKAGE_NAME + "-compare");

        transaction.commit();
    }

    public void Merge (String name, ArrayList<String> ids, String listItem) {
        FragmentManager fragMan = getFragmentManager();
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
        transaction.replace(R.id.container, newFragment, PACKAGE_NAME + "-merge");

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         * The action bar home/up action should open or close the drawer.
         * mDrawerToggle will take care of this.
         */
        if (mNavigationDrawerFragment.mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
