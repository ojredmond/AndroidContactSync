package uk.me.redmonds.contactsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SyncFragment extends ListFragment {
    public static final String MATCH = "Perform Matching";
    public static final String FULL = "Full Sync";
    public static final String SYNC = "Sync";
    public static final String OPTIONS = "options";
    public static final String SUMMARY = "summary";
    public static final String DUP = "Duplicates from Account";
    public static final String UNMATCHED = "Unmatched from Account";
    public static final String MATCHED = "Review matches";
    private static final String LAST = "View Last Matched Results";
    private static final String NODUP = "No Duplicates";
    private ArrayList<HashMap<String, Object>> values = new ArrayList<>();
    private MainActivity main;
    private SharedPreferences pref;
    private SharedPreferences settings;
    private String list_type;
    //private String list_item;
    private String account1Name;
    private String account2Name;
    private String dup1;
    private String dup2;
    private String un1;
    private String un2;
    private HashSet<String> dup1Name;
    private HashSet<String> dup2Name;
    private HashMap<String, Object> value;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        values.clear();
        Bundle args = getArguments();
        list_type = args.getString("list_type", null);
        //list_item = args.getString("list_item", null);

        main = (MainActivity)this.getActivity();

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
            value = new HashMap<>();
            value.put(FlexibleListAdapter.TITLE, MATCH);
            if (account1Name == null || account2Name == null) {
                value.put(FlexibleListAdapter.DESCRIPTION, "Accounts need to be set");
            } else if (account1Name.equals(account2Name)) {
                value.put(FlexibleListAdapter.DESCRIPTION, "for " + account1Name);
            } else {
                value.put(FlexibleListAdapter.DESCRIPTION, "for " + account1Name + " & " + account2Name);
            }

            values.add(value);

            if (pref.getBoolean(Match.SYNCMATCHED,false)) {
                value = new HashMap<>();
                value.put(FlexibleListAdapter.TITLE, LAST);
                values.add(value);
            }
        } else if (list_type.equals(SUMMARY)) {
            if (account1Count == 0 && account2Count == 0) {
                value = new HashMap<>();
                value.put(FlexibleListAdapter.TITLE, "ERROR");
                value.put(FlexibleListAdapter.DESCRIPTION, "Their are no contacts in either account");
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
            value = new HashMap<>();
            value.put(FlexibleListAdapter.TITLE, FULL);
            values.add(value);
            //add logic to detect a full sync has been performed
            value = new HashMap<>();
            value.put(FlexibleListAdapter.TITLE, SYNC);
            values.add(value);
        }

        
        FlexibleListAdapter adapter = new FlexibleListAdapter(values.toArray(new HashMap[values.size()]), main, android.R.layout.simple_list_item_2);
        
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		Toast.makeText(v.getContext(),""+l.getAdapter().getItem(position).toString(),Toast.LENGTH_LONG).show();
		
		HashMap<String,Object> clickedItem = (HashMap<String,Object>)l.getAdapter().getItem(position);
		
        if (clickedItem.get(FlexibleListAdapter.TITLE).equals(MATCH)) {
            settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            account1Name = settings.getString(MainActivity.ACCOUNT1, null);
            account2Name = settings.getString(MainActivity.ACCOUNT2, null);

            if (account1Name == null || account2Name == null) {
                Toast.makeText(v.getContext(),"Can not find 2 accounts set, please go to settings",Toast.LENGTH_LONG).show();
                return;
            }

            main.matchStatus();
        } else if (clickedItem.get(FlexibleListAdapter.TITLE).equals(LAST)) {
            main.showResults();
		} else if (clickedItem.containsKey(FlexibleListAdapter.LISTITEM)) {
			main.Compare(list_type, (String)clickedItem.get(FlexibleListAdapter.LISTITEM), null);
		
		/*} else if (v.getTag() != null && v.getTag() instanceof String) {
			Toast.makeText(v.getContext(),v.getTag(),Toast.LENGTH_LONG).show();
			//main.Compare(list_type, (String)v.getTag(), null);
        } else if (((String)values.get((int) id).get(FlexibleListAdapter.TITLE)).startsWith(DUP)) {
            switch (((String)values.get((int) id).get(FlexibleListAdapter.TITLE)).charAt(DUP.length())) {
                case '1':
                    //main.showResults(dup1);
                    main.Compare(list_type, dup1, null);
                    break;
                case '2':
                    //main.showResults(dup2);
                    main.Compare(list_type, dup2, null);
                    break;
                default:
                    Toast.makeText(v.getContext(), id + ":" + values.get((int) id) + ":" + ((String)values.get((int) id).get(FlexibleListAdapter.TITLE)).charAt(DUP.length()) + ":", Toast.LENGTH_LONG).show();
            }
        } else if (((String)values.get((int) id).get(FlexibleListAdapter.TITLE)).startsWith(UNMATCHED)) {
            switch (((String)values.get((int) id).get(FlexibleListAdapter.TITLE)).charAt(UNMATCHED.length())) {
                case '1':
                    main.Compare(list_type, un1, null);
                    break;
                case '2':
                    main.Compare(list_type, un2, null);
                    break;
                default:
                    Toast.makeText(v.getContext(), id + ":" + values.get((int) id) + ":" + ((String)values.get((int) id).get(FlexibleListAdapter.TITLE)).charAt(DUP.length()) + ":", Toast.LENGTH_LONG).show();
            }
        } else if (((String)values.get((int) id).get(FlexibleListAdapter.TITLE)).startsWith(MATCHED)) {
            main.Compare(list_type, Match.MATCHEDKEY + account1Name + ":" + account2Name, null);
        //} else if (list_item != null && list_item.startsWith("dup")) {
        //    main.Compare(list_type, list_item, ((String)values.get((int) id).get(FlexibleListAdapter.TITLE)));
        } else if (((String)values.get((int) id).get(FlexibleListAdapter.TITLE)).startsWith(NODUP)) {
            //do nothing*/
        } else {
            Toast.makeText(v.getContext(), values.get((int) id).get(FlexibleListAdapter.TITLE), Toast.LENGTH_LONG).show();
        }
    }

    private void showSummary () {
        un1 = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
        un2 = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;

        HashSet<String> unmatched1Name = (HashSet<String>)pref.getStringSet(un1, null);
        HashSet<String> unmatched2Name = (HashSet<String>)pref.getStringSet(un2, null);
        HashSet<String> matched1 = (HashSet<String>)pref.getStringSet(Match.MATCHEDKEY + account1Name + ":" + account2Name, null);

        Boolean dup = false;
        HashSet<HashMap<String, Object>> dupValues = new HashSet<>();
        for (String type : Match.MIME_TYPE_LIST) {
            String dupLabel = Match.DUPKEY + type + account1Name;
            HashSet<String> dupSet = (HashSet<String>) pref.getStringSet(dupLabel, null);
            if (dupSet != null && dupSet.size() > 0) {
                dup = true;
                value = new HashMap<>();
                value.put(FlexibleListAdapter.TITLE, Contacts.getGroupName(type) + " (" + dupSet.size() + ")");
                value.put(FlexibleListAdapter.LAYOUT, android.R.layout.simple_list_item_1);
                value.put(FlexibleListAdapter.LISTITEM, dupLabel);
                dupValues.add(value);
            }
        }

        if (dup) {
            value = new HashMap<>();
            value.put(FlexibleListAdapter.TITLE, DUP + "1");
            value.put(FlexibleListAdapter.DESCRIPTION, account1Name);
            values.add(value);
            values.addAll(dupValues);
        } else if (account1Name.equals(account2Name)) {
            value = new HashMap<>();
            value.put(FlexibleListAdapter.TITLE, NODUP);
            value.put(FlexibleListAdapter.DESCRIPTION, account1Name);
            values.add(value);
        }

        if (!account1Name.equals(account2Name) && dup2Name != null && dup2Name.size() > 0) {
            value = new HashMap<>();
            value.put(FlexibleListAdapter.TITLE, DUP + "2 (" + dup2Name.size() + ")");
            value.put(FlexibleListAdapter.DESCRIPTION, account2Name);
			value.put(FlexibleListAdapter.LISTITEM, dup2Name);
            values.add(value);
        }

        if (unmatched1Name != null && unmatched1Name.size() > 0) {
            value = new HashMap<>();
            value.put(FlexibleListAdapter.TITLE, UNMATCHED + "1 (" + unmatched1Name.size() + ")");
            value.put(FlexibleListAdapter.DESCRIPTION, account1Name);
			value.put(FlexibleListAdapter.LISTITEM, unmatched1Name);
            values.add(value);
        }

        if (unmatched2Name != null && unmatched2Name.size() > 0) {
            value = new HashMap<>();
            value.put(FlexibleListAdapter.TITLE, UNMATCHED + "2 (" + unmatched2Name.size() + ")");
            value.put(FlexibleListAdapter.DESCRIPTION, account2Name);
			value.put(FlexibleListAdapter.LISTITEM, unmatched2Name);
            values.add(value);
        }

        if (!account1Name.equals(account2Name) && matched1 != null && matched1.size() > 0) {
            value = new HashMap<>();
            value.put(FlexibleListAdapter.TITLE, MATCHED + " (" + matched1.size() + ")");
            value.put(FlexibleListAdapter.DESCRIPTION, account1Name + " " + account2Name);
			value.put(FlexibleListAdapter.LISTITEM, matched1);
            values.add(value);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        //set actionbar title
        ((MainActivity)getActivity()).setHeading(getString(R.string.app_name));
    }
}
