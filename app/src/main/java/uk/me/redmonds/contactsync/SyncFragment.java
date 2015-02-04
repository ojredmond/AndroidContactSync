package uk.me.redmonds.contactsync;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;
import java.util.*;

import android.app.ListFragment;

public class SyncFragment extends ListFragment {
    //private ArrayList<String> values = new ArrayList<String>();
    private List<Map<String, String>> values = new ArrayList<Map<String, String>>();
    private MainActivity main;
    private SharedPreferences pref;
    private SharedPreferences settings;
    private String list_type;
    private String list_item;
    public static final String MATCH = "Perform Matching";
    public static final String FULL = "Full Sync";
    public static final String SYNC = "Sync";
    private static final String LAST = "View Last Matched Results";
    public static final String OPTIONS = "options";
    public static final String SUMMARY = "summary";
    public static final String DUP = "Duplicates from Account";
    public static final String UNMATCHED = "Unmatched from Account";
    public static final String MATCHED = "Review matches";
    private String account1Name;
    private String account2Name;
    private String dup1;
    private String dup2;
    private String un1;
    private String un2;
    private HashSet<String> dup1Name;
    private HashSet<String> dup2Name;
    private static final String NODUP = "No Duplicates";
    private final static String NAME = "Name";
    private final static String DESCRIPTION = "Desc";

    private HashMap<String, String> value;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        values.clear();
        Bundle args = getArguments();
        list_type = args.getString("list_type", null);
        list_item = args.getString("list_item", null);

        main = (MainActivity)this.getActivity();
        //main.showMenuIcon();
        ActionBar actionBar = main.getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setNavigationMode(ActionBar.DISPLAY_HOME_AS_UP);
        //actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.show();

        pref = main.getPreferences(Context.MODE_PRIVATE);
        settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        account1Name = settings.getString(MainActivity.ACCOUNT1, null);
        account2Name = settings.getString(MainActivity.ACCOUNT2, null);

        // get he number of contacts in each account to chek they are not both empty
        int account1Count = pref.getInt(Match.NUMCONTACTS + account1Name, -1);
        int account2Count = pref.getInt(Match.NUMCONTACTS + account2Name, -1);

        //get duplicate lists for display desicisions
        dup1 = Match.DUPKEY + account1Name;
        dup2 = Match.DUPKEY + account2Name;
        dup1Name = (HashSet<String>)pref.getStringSet(dup1, null);
        dup2Name = (HashSet<String>)pref.getStringSet(dup2, null);

        if (list_type.equals(OPTIONS) || !pref.getBoolean(Match.SYNCMATCHED,false)) {
            //actionBar.setTitle(title);
            //actionBar.setDisplayHomeAsUpEnabled(false);
            value = new HashMap <String, String>();
            value.put(NAME,MATCH);
            if (account1Name == null || account2Name == null) {
                value.put(DESCRIPTION, "Accounts need to be set");
            } else if (account1Name.equals(account2Name)) {
                value.put(DESCRIPTION, "for " + account1Name);
            } else {
                value.put(DESCRIPTION, "for " + account1Name + " & " + account2Name);
            }

            values.add(value);

            if (pref.getBoolean(Match.SYNCMATCHED,false)) {
                value = new HashMap <String, String>();
                value.put(NAME,LAST);
                values.add(value);
            }
        } else if (list_type.equals(SUMMARY)) {
            //actionBar.setTitle(R.string.title_activity_results);
            if (list_item != null && list_item.startsWith("dup")) {
                showDuplicates(list_item);
            } else if (account1Count == 0 && account2Count == 0) {
                value = new HashMap <String, String>();
                value.put(NAME, "ERROR");
                value.put(DESCRIPTION, "Their are no contacts in either account");
                values.add(value);
            } else {
                showSummary();
            }
        }


        if (account1Name != null && account2Name != null
                && !account1Name.equals(account2Name)
                && pref.getBoolean(Match.SYNCMATCHED,false)
                && (dup1Name != null && dup1Name.size() > 0)
                && (dup2Name != null && dup2Name.size() > 0)) {
            value = new HashMap <String, String>();
            value.put(NAME,FULL);
            values.add(value);
            //add logic to detect a full sync has been performed
            value = new HashMap <String, String>();
            value.put(NAME,SYNC);
            values.add(value);
        }

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(main, android.R.layout.simple_list_item_1, values);
        SimpleAdapter adapter = new SimpleAdapter(main,
                values,
                android.R.layout.simple_list_item_2,
                new String[] { NAME, DESCRIPTION },
                new int[] { android.R.id.text1, android.R.id.text2 });
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (values.get((int)id).get(NAME) == MATCH) {
            settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            account1Name = settings.getString(MainActivity.ACCOUNT1, null);
            account2Name = settings.getString(MainActivity.ACCOUNT2, null);

            if (account1Name == null || account2Name == null) {
                Toast.makeText(v.getContext(),"Can not find 2 accounts set, please go to settings",Toast.LENGTH_LONG).show();
                return;
            }

            main.matchStatus(values.get((int)id).get(NAME));
        } else if (values.get((int)id).get(NAME) == LAST) {
            main.showResults();
        } else if (values.get((int)id).get(NAME).startsWith(DUP)) {
            switch (values.get((int)id).get(NAME).charAt(DUP.length())) {
                case '1':
                    //main.showResults(dup1);
                    main.Compare(list_type, dup1, null);
                    break;
                case '2':
                    //main.showResults(dup2);
                    main.Compare(list_type, dup2, null);
                    break;
                default:
                    Toast.makeText(v.getContext(),id + ":" + values.get((int)id) + ":" + values.get((int)id).get(NAME).charAt(DUP.length()) + ":",Toast.LENGTH_LONG).show();
            }
        } else if (values.get((int)id).get(NAME).startsWith(UNMATCHED)) {
            switch (values.get((int)id).get(NAME).charAt(UNMATCHED.length())) {
                case '1':
                    main.Compare(list_type, un1, null);
                    break;
                case '2':
                    main.Compare(list_type, un2, null);
                    break;
                default:
                    Toast.makeText(v.getContext(),id + ":" + values.get((int)id) + ":" + values.get((int)id).get(NAME).charAt(DUP.length()) + ":",Toast.LENGTH_LONG).show();
            }
        } else if (values.get((int)id).get(NAME).startsWith(MATCHED)) {
            main.Compare(list_type, Match.MATCHEDKEY + account1Name + ":" + account2Name, null);
        } else if (list_item != null && list_item.startsWith("dup")) {
            main.Compare(list_type, list_item, values.get((int)id).get(NAME));
        } else if (values.get((int)id).get(NAME).startsWith(NODUP)) {
            //do nothing
        } else {
            Toast.makeText(v.getContext(),values.get((int)id).get(NAME),Toast.LENGTH_LONG).show();
        }
    }

    private void showSummary () {
        un1 = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
        un2 = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;

        HashSet<String> unmatched1Name = (HashSet<String>)pref.getStringSet(un1, null);
        HashSet<String> unmatched2Name = (HashSet<String>)pref.getStringSet(un2, null);
        HashSet<String> matched1 = (HashSet<String>)pref.getStringSet(Match.MATCHEDKEY + account1Name + ":" + account2Name, null);

        if (dup1Name != null && dup1Name.size() > 0) {
            value = new HashMap <String, String>();
            value.put(NAME,DUP + "1 (" + dup1Name.size() + ")");
            value.put(DESCRIPTION, account1Name);
            values.add(value);
        } else if (account1Name.equals(account2Name)) {
            value = new HashMap <String, String>();
            value.put(NAME,NODUP);
            value.put(DESCRIPTION, account1Name);
            values.add(value);
        }

        if (!account1Name.equals(account2Name) && dup2Name != null && dup2Name.size() > 0) {
            value = new HashMap <String, String>();
            value.put(NAME,DUP + "2 (" + dup2Name.size() + ")");
            value.put(DESCRIPTION, account2Name);
            values.add(value);
        }

        if (unmatched1Name != null && unmatched1Name.size() > 0) {
            value = new HashMap <String, String>();
            value.put(NAME,UNMATCHED + "1 (" + unmatched1Name.size() + ")");
            value.put(DESCRIPTION, account1Name);
            values.add(value);
        }

        if (unmatched2Name != null && unmatched2Name.size() > 0) {
            value = new HashMap <String, String>();
            value.put(NAME,UNMATCHED + "2 (" + unmatched2Name.size() + ")");
            value.put(DESCRIPTION, account2Name);
            values.add(value);
        }

        if (!account1Name.equals(account2Name) && matched1 != null && matched1.size() > 0) {
            value = new HashMap <String, String>();
            value.put(NAME,MATCHED + " (" + matched1.size() + ")");
            value.put(DESCRIPTION, account1Name + " " + account2Name);
            values.add(value);
        }
    }

    public void showDuplicates(String item) {
        StringList dup = new StringList(pref, item);
        //values.addAll(dup.getHashSet());
    }
    
    @Override
    public void onAttach (Activity activity) {
        super.onAttach (activity);
        //set actionbar title
        ((MainActivity)activity).onSectionAttached(getString(R.string.app_name));
    }
}
