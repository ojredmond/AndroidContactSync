package uk.me.redmonds.contactsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.preference.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends ActionBarActivity
        implements StatusFragment.OnViewCreatedListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String ACCOUNT_TYPE = "com.google";
    public static final String ACCOUNT1 = "account1";
    public static final String ACCOUNT2 = "account2";
    public static final String GROUPS = "GroupsOnOff";
    public static final String PHOTOS = "PicturesOnOff";
    public static final String DEEP = "DeepOnOff";
    public static final String PACKAGE_NAME = "uk.me.redmonds.contactsync";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private FragmentManager fragmentManager;
    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle = "";
    private StatusFragment log;
    private String logText = "";
    private String syncType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name);
        mTitle = getString(R.string.app_name);

        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));

        //get the fragment manager
        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            if (savedInstanceState.containsKey("log")) {
                logText = savedInstanceState.getString("log");
            }

        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                fragmentManager.findFragmentById(R.id.navigation_drawer);


        // Set up the drawer.
        mNavigationDrawerFragment.setUp(actionBar,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        String trace = "";
        String line;
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(this.openFileInput("stack.trace")));
            while ((line = reader.readLine()) != null) {
                trace += line + "\n";
            }
        } catch (IOException ioe) {
            return;
        }

        TopExceptionHandler.sendReport(this, trace);

        this.deleteFile("stack.trace");
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (fragmentManager == null) fragmentManager = getSupportFragmentManager();

        // update the main content by replacing fragments
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new SettingsFragment(), PACKAGE_NAME + "-" + getString(R.string.title_settings))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                break;
            case 1:
                showOptions();
                break;
            case 2:
                String tag = PACKAGE_NAME + "-" + getString(R.string.title_logs);
                if (log == null) {
                    Bundle args = new Bundle();
                    args.putString("logText", logText);
                    log = new StatusFragment();
                    log.setArguments(args);
                }

                fragmentManager.beginTransaction()
                        .replace(R.id.container, log, tag)
                        .commit();
                break;
            case 3:
                showResults();
                break;
        }
    }

    public void setHeading(CharSequence title) {
        if (!title.equals("")) mTitle = title;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);
    }

    public void matchStatus(String type) {
        syncType = type;
        onNavigationDrawerItemSelected(2);
    }

    public void onViewCreated(StatusFragment status) {
        if(syncType.equals(SyncFragment.MATCH)) {
            Match m = new Match();
            m.startMatch(this, status, syncType);
        } else if(syncType.equals(SyncFragment.FULL)) {
            Sync s = new Sync();
            s.startSync(this, status, syncType);
        }

        syncType = "";
    }

    void showOptions() {
        showList(SyncFragment.OPTIONS, PACKAGE_NAME + "-" + getString(R.string.title_sync));
        //update the navigation drawer
        if (mNavigationDrawerFragment != null)
            mNavigationDrawerFragment.changeItem(1);
    }

    public void showResults() {
		if(!isChangingConfigurations()&&!isRestricted()&&!isFinishing() && !isDestroyed()) {
			showList(SyncFragment.SUMMARY, PACKAGE_NAME + "-" + getString(R.string.title_results));
			//update the navigation drawer
			if (mNavigationDrawerFragment != null)
				mNavigationDrawerFragment.changeItem(3);
		}
    }

    void showList(String type, String fragTag) {
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
        transaction.replace(R.id.container, newFragment, fragTag);

        transaction.commit();
    }

    public void Compare(String listType, String listItem, String selected) {
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

        transaction.commit();
    }

    public void Merge(String name, String[] ids, String listItem) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        MergeFragment newFragment = new MergeFragment();

        // Pass what list to show
        Bundle args = new Bundle();
        if (name != null) {
            args.putString("selected", name);
        }
        if (ids != null) {
            args.putStringArray("ids", ids);
        }
        if (listItem != null) {
            args.putString("listItem", listItem);
        }

        newFragment.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        transaction.replace(R.id.container, newFragment, PACKAGE_NAME + "-merge");

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
        //check to see if drawer id open
        if (mNavigationDrawerFragment.isVisible())
            mNavigationDrawerFragment.closeDrawer();
        else if (fragmentManager.findFragmentById(R.id.container) != null) {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.container);
            String currentFragmentClass = currentFragment.getClass().getName();
            String type, item, name;

            switch (currentFragmentClass) {
                case "uk.me.redmonds.contactsync.SyncFragment":
                    type = (String) currentFragment.getArguments().get("list_type");

                    if (type.equals(SyncFragment.SUMMARY))
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
                    item = (String) currentFragment.getArguments().get("listItem");
                    type = (String) currentFragment.getArguments().get("listType");
                    name = (String) currentFragment.getArguments().get("selected");
                    Compare(type, item, name);
                    break;
                default:
                    Toast.makeText(this, currentFragmentClass, Toast.LENGTH_SHORT).show();
                    super.onBackPressed();
            }
        } else
            super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (log != null && !log.getLog().equals(""))
            outState.putString("log", log.getLog());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy()
    {
        getSharedPreferences(StatusFragment.LOG_TAG, Context.MODE_PRIVATE).edit().clear().apply();
        
        super.onDestroy();
    }
}
