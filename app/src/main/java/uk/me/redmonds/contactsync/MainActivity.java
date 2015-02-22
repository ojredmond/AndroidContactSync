package uk.me.redmonds.contactsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity
        implements StatusFragment.OnViewCreatedListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String ACCOUNT_TYPE = "com.google";
    public static final String ACCOUNT1 = "account1";
    public static final String ACCOUNT2 = "account2";
    public static final String GROUPS = "GroupsOnOff";
    public static final String PHOTOS = "PicturesOnOff";
	public static final String DEEP = "DeepOnOff";
    public static String PACKAGE_NAME;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    NavigationDrawerFragment mNavigationDrawerFragment;
    private FragmentManager fragmentManager;
    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle = "";
    private String syncType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getApplicationContext().getPackageName();
	
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name);
        mTitle = getString(R.string.app_name);
		
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
		
		//get the fragment manager
		fragmentManager = getSupportFragmentManager();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
			fragmentManager.findFragmentById(R.id.navigation_drawer);
        

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
        } catch(IOException ioe) {
            return;
        }

        TopExceptionHandler.sendReport (this, trace);

        this.deleteFile("stack.trace");
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
		if(fragmentManager == null) fragmentManager = getSupportFragmentManager();
		
        // update the main content by replacing fragments
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new SettingsFragment(), PACKAGE_NAME + "-" + getString(R.string.title_settings))
                        .commit();
                break;
            case 1:
                showOptions();
                break;
            case 2:
            	if (!restoreFragment(PACKAGE_NAME + "-" + getString(R.string.title_logs))) {
                    fragmentManager.beginTransaction()
                        .replace(R.id.container, new StatusFragment(), PACKAGE_NAME + "-" + getString(R.string.title_logs))
                        .commit();
            	}
                break;
            case 3:
                showResults();
                break;
        }
    }

    private Boolean restoreFragment(String fragTag) {
        Fragment frag = fragmentManager.findFragmentByTag(fragTag);
    	if (frag == null) return false;

        fragmentManager.beginTransaction()
            .replace(R.id.container, frag, fragTag)
            .commit();
            
        return true;
    }
    public void setHeading(CharSequence title) {
        if(!title.equals("")) mTitle = title;
		
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);
		
    }

    public void matchStatus() {
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
        results.apply();

        // Create fragment and give it an argument specifying the article it should show
        StatusFragment newFragment = new StatusFragment();

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.container, newFragment, PACKAGE_NAME + "-" + getString(R.string.title_logs));
        //transaction.addToBackStack(PACKAGE_NAME + "-" + getString(R.string.title_logs));

        // Commit the transaction
        transaction.commit();
    }

    public void onViewCreated(View statusView) {
        Match m = new Match();
        m.startMatch(this, statusView, syncType);
    }

    public void showOptions () {
        showList(SyncFragment.OPTIONS);
    }
    
    public void showResults () {
        showList(SyncFragment.SUMMARY);
    }

    public void showList (String type) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        SyncFragment newFragment = new SyncFragment();

        // Pass what list to show
        Bundle args = new Bundle();
        if (type != null)
            args.putString("list_type", type);
        else
            args.putString("list_type", SyncFragment.OPTIONS);

        newFragment.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        transaction.replace(R.id.container, newFragment, PACKAGE_NAME + "-" + getString(R.string.title_results));
        //transaction.addToBackStack(PACKAGE_NAME + "-" + getString(R.string.title_results));

        transaction.commit();
        
        //update the navigation drawer
        if(mNavigationDrawerFragment != null)
            mNavigationDrawerFragment.changeItem(3);
    }

    public void Compare (String listType, String listItem, String selected) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
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

        //transaction.addToBackStack(PACKAGE_NAME + "-compare");

        transaction.commit();
    }

    public void Merge (String name, String[] ids, String listItem) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        MergeFragment newFragment = new MergeFragment();

        // Pass what list to show
        Bundle args = new Bundle();
        if (name != null) {
            args.putString("name", name);
        }
        if (ids != null) {
			args.putStringArray("ids",ids);
            //args.putStringArrayList("ids", ids);
        }
        if (listItem != null) {
            args.putString("listItem", listItem);
        }

        SharedPreferences.Editor pref = getPreferences(Context.MODE_PRIVATE).edit();
        pref.remove("contactMerge");
        pref.remove("contactsMerge");
        pref.apply();

        newFragment.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        transaction.replace(R.id.container, newFragment, PACKAGE_NAME + "-merge");

        //transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         * The action bar home/up action should open or close the drawer.
         * mDrawerToggle will take care of this.
         */
        return mNavigationDrawerFragment.mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        
        if(fragmentManager.findFragmentById(R.id.container)!=null) {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.container);
            String currentFragmentClass = currentFragment.getClass().getName();
            String type, item, name;

            switch (currentFragmentClass) {
                case "uk.me.redmonds.contactsync.SyncFragment":
                    type = (String)currentFragment.getArguments().get("list_type");

                    if(type.equals(SyncFragment.SUMMARY))
                        showOptions();
                    else
                        super.onBackPressed();
                    break;
                case "uk.me.redmonds.contactsync.Settings_fragment":
                    showOptions();
                    break;
                case "uk.me.redmonds.contactsync.CompareFragment":
                    showResults();
                    break;
                case "uk.me.redmonds.contactsync.MergeFragment":
                    item = (String)currentFragment.getArguments().get("listItem");
                    type = (String) currentFragment.getArguments().get("listType");
                    name = (String) currentFragment.getArguments().get("name");
                    Compare(type, item, name);
                    break;
                default:
                    Toast.makeText(this,currentFragmentClass,Toast.LENGTH_SHORT).show();
                    super.onBackPressed();
            }
        } else
            super.onBackPressed();
    }

}
