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
    public static final String OPTIONS = "options";
    public static final String SUMMARY = "summary";
    private static final String MATCH = "Perform Matching";
    private static final String FULL = "Full Sync";
    private static final String SYNC = "Sync Changes";
    private static final String DUP = "Duplicates";
    private static final String UNMATCHED = "Unmatched";
    private static final String PMATCHED = "Potential Matches";
    private static final String MATCHED = "Review matches";
    private static final String LAST = "View Last Results";
    private static final String NODUP = "No Duplicates";
    private final ArrayList<StringMap> values = new ArrayList<>();
    private MainActivity main;
    private SharedPreferences prefMatch;
	private SharedPreferences prefAccount1;
	private SharedPreferences prefAccount2;
    private SharedPreferences settings;
    private String list_type;
    private String account1Name;
    private String account2Name;
    private StringMap value;
    private FlexibleListAdapter adapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        values.clear();
        Bundle args = getArguments();
        list_type = args.getString("list_type", null);

        main = (MainActivity) this.getActivity();

        settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        account1Name = settings.getString(MainActivity.ACCOUNT1, null);
        account2Name = settings.getString(MainActivity.ACCOUNT2, null);
		String accountsKey;
		int account1Count = 0;
		int account2Count = 0;
		if(account1Name != null && account2Name != null) {
			if(account1Name.compareTo(account2Name) > 0)
				accountsKey = account1Name + account2Name;
			else
				accountsKey = account2Name + account1Name;

			prefAccount1 = main.getSharedPreferences(Match.PREF_KEY_ACCOUNT+account1Name, Context.MODE_PRIVATE);
			prefAccount2 = main.getSharedPreferences(Match.PREF_KEY_ACCOUNT+account2Name, Context.MODE_PRIVATE);
			prefMatch = main.getSharedPreferences(Match.PREF_KEY_MATCH+accountsKey, Context.MODE_PRIVATE);

			// get he number of contacts in each account to chek they are not both empty
			account1Count = prefAccount1.getInt(Match.NUMCONTACTS + account1Name, -1);
			account2Count = prefAccount2.getInt(Match.NUMCONTACTS + account2Name, -1);
		}
		
        if (list_type.equals(OPTIONS) || !prefMatch.getBoolean(Match.SYNCMATCHED, false)) {
            value = new StringMap();
            value.put(FlexibleListAdapter.TEXT, new String[]{"Match"});
            value.put(FlexibleListAdapter.LAYOUT, R.layout.heading_surround);
            value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.heading});
            values.add(value);
            value = new StringMap();

            if (account1Name == null || account2Name == null) {
                value.put(FlexibleListAdapter.TEXT, new String[]{MATCH, "Accounts need to be set"});
            } else if (account1Name.equals(account2Name)) {
                value.put(FlexibleListAdapter.TEXT, new String[]{MATCH, "for " + account1Name});
            } else {
                value.put(FlexibleListAdapter.TEXT, new String[]{MATCH, "for " + account1Name + " & " + account2Name});
            }
            value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_none_r2);
            value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.title, R.id.value});
            values.add(value);

            if (prefMatch != null && prefMatch.getBoolean(Match.SYNCMATCHED, false)) {
                value = new StringMap();
                value.put(FlexibleListAdapter.TEXT, new String[]{LAST});
                value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_top_r2);
                value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.title});
                values.add(value);
            }
        } else if (list_type.equals(SUMMARY)) {
            if (account1Count == 0 && account2Count == 0) {
                value = new StringMap();
                value.put(FlexibleListAdapter.TITLE, "ERROR");
                value.put(FlexibleListAdapter.DESCRIPTION, "Their are no contacts in either account");
                values.add(value);
            } else {
                showSummary();
            }
        }

        Boolean hideSync = false;
		if(account1Name != null && account2Name != null) {
			for (String type : Match.MIME_TYPE_LIST) {
				String label = Match.DUPKEY + type + account1Name;
				HashSet<String> set = (HashSet<String>) prefAccount1.getStringSet(label, null);
				if (set != null && set.size() > 0) {
					hideSync = true;
				}
				label = Match.DUPKEY + type + account2Name;
				set = (HashSet<String>) prefAccount2.getStringSet(label, null);
				if (set != null && set.size() > 0) {
					hideSync = true;
				}
				label = Match.MATCHEDKEY + type + account1Name + ":" + account2Name;
				set = (HashSet<String>) prefMatch.getStringSet(label, null);
				if (set != null && set.size() > 0) {
					hideSync = true;
				}
				label = Match.MATCHEDKEY + type + account2Name + ":" + account1Name;
				set = (HashSet<String>) prefMatch.getStringSet(label, null);
				if (set != null && set.size() > 0) {
					hideSync = true;
				}
			}
		}
        
        if (account1Name != null && account2Name != null
                && !account1Name.equals(account2Name)
				&& prefMatch.getBoolean(Match.SYNCMATCHED, false)
                && !hideSync) {
            value = new StringMap();
            value.put(FlexibleListAdapter.TEXT, new String[]{"Sync"});
            value.put(FlexibleListAdapter.LAYOUT, R.layout.heading_surround);
            value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.heading});
            values.add(value);
            value = new StringMap();
            value.put(FlexibleListAdapter.TEXT, new String[]{FULL});
            value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_none_2);
            value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.value});
            values.add(value);
            //add logic to detect a full sync has been performed
            value = new StringMap();
            value.put(FlexibleListAdapter.TEXT, new String[]{SYNC});
            value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_top_2);
            value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.value});
            values.add(value);
        }


        adapter = new FlexibleListAdapter(values.toArray(new StringMap[values.size()]), main);

        setListAdapter(adapter);
        ListView lv = getListView();
        lv.setDivider(null);
        int gap = getResources().getDimensionPixelOffset(R.dimen.small_gap);
        lv.setPadding(0, gap, 0, 0);
        View top = main.getLayoutInflater().inflate(R.layout.spacer, lv, false);
        lv.addHeaderView(top);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //HashMap<String, Object> clickedItem = ((FlexibleListAdapter) l.getAdapter()).getItem(position);
        HashMap<String, Object> clickedItem = adapter.getItem(position - 1);

        if (clickedItem.containsKey(FlexibleListAdapter.TEXT) && ((String[]) clickedItem.get(FlexibleListAdapter.TEXT))[0].equals(MATCH)) {
            settings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            account1Name = settings.getString(MainActivity.ACCOUNT1, null);
            account2Name = settings.getString(MainActivity.ACCOUNT2, null);

            if (account1Name == null || account2Name == null) {
                Toast.makeText(v.getContext(), "Can not find 2 accounts set, please go to settings", Toast.LENGTH_LONG).show();
                return;
            }
			
			prefMatch.edit().putBoolean(Match.SYNCMATCHED, false).apply();
            main.matchStatus();
        } else if (clickedItem.containsKey(FlexibleListAdapter.TEXT) && ((String[]) clickedItem.get(FlexibleListAdapter.TEXT))[0].equals(LAST)) {
            main.showResults();
        } else if (clickedItem.containsKey(FlexibleListAdapter.LISTITEM)) {
            main.Compare(list_type, (String) clickedItem.get(FlexibleListAdapter.LISTITEM), null);
        } else {
            Toast.makeText(v.getContext(), (String) values.get((int) id).get(FlexibleListAdapter.TITLE), Toast.LENGTH_LONG).show();
        }
    }

    private void showSummary() {
        String un1 = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
        String un2 = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;

        HashSet<String> unmatched1Name = (HashSet<String>) prefMatch.getStringSet(un1, null);
        HashSet<String> unmatched2Name = (HashSet<String>) prefMatch.getStringSet(un2, null);
        HashSet<String> matched1 = (HashSet<String>) prefMatch.getStringSet(Match.MATCHEDKEY + account1Name + ":" + account2Name, null);

        Boolean first = true;
        Boolean dup = false;
        for (String type : Match.MIME_TYPE_LIST) {
            String dupLabel = Match.DUPKEY + type + account1Name;
            HashSet<String> dupSet = (HashSet<String>) prefAccount1.getStringSet(dupLabel, null);
            if (dupSet != null && dupSet.size() > 0) {
                if (!dup) {
                    dup = true;
                    value = new StringMap();
                    value.put(FlexibleListAdapter.TEXT, new String[]{DUP});
                    value.put(FlexibleListAdapter.LAYOUT, R.layout.heading_surround);
                    value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.heading});
                    values.add(value);
                    value = new StringMap();
                    value.put(FlexibleListAdapter.TEXT, new String[]{"Account", account1Name});
                    value.put(FlexibleListAdapter.LAYOUT, R.layout.list_account);
                    value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.type, R.id.value});
                    values.add(value);
                }

                value = new StringMap();
                value.put(FlexibleListAdapter.TEXT, new String[]{ContactsHelper.getGroupName(type), "(" + dupSet.size() + ")"});
                if (first)
                    value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_none_2);
                else
                    value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_top_2);

                value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.type, R.id.value});
                value.put(FlexibleListAdapter.LISTITEM, dupLabel);
                values.add(value);
                first = false;
            }
        }

        if (!dup && account1Name.equals(account2Name)) {
            value = new StringMap();
            value.put(FlexibleListAdapter.TITLE, NODUP);
            value.put(FlexibleListAdapter.DESCRIPTION, account1Name);
            values.add(value);
        }

        first = true;
        if (!account1Name.equals(account2Name)) {
            for (String type : Match.MIME_TYPE_LIST) {
                String dupLabel = Match.DUPKEY + type + account2Name;
                HashSet<String> dupSet = (HashSet<String>) prefAccount2.getStringSet(dupLabel, null);
                if (dupSet != null && dupSet.size() > 0) {
                    if (!dup) {
                        dup = true;
                        value = new StringMap();
                        value.put(FlexibleListAdapter.TEXT, new String[]{DUP});
                        value.put(FlexibleListAdapter.LAYOUT, R.layout.heading_surround);
                        value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.heading});
                        values.add(value);
                    }
                    if (first) {
                        value = new StringMap();
                        value.put(FlexibleListAdapter.TEXT, new String[]{"Account", account2Name});
                        value.put(FlexibleListAdapter.LAYOUT, R.layout.list_account);
                        value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.type, R.id.value});
                        values.add(value);
                    }

                    value = new StringMap();
                    value.put(FlexibleListAdapter.TEXT, new String[]{ContactsHelper.getGroupName(type), "(" + dupSet.size() + ")"});
                    if (first)
                        value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_none_2);
                    else
                        value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_top_2);
                    value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.type, R.id.value});
                    value.put(FlexibleListAdapter.LISTITEM, dupLabel);
                    values.add(value);
                    first = false;
                }
            }

            if ((unmatched1Name != null && unmatched1Name.size() > 0)
                    || (unmatched2Name != null && unmatched2Name.size() > 0)) {
                value = new StringMap();
                value.put(FlexibleListAdapter.TEXT, new String[]{UNMATCHED});
                value.put(FlexibleListAdapter.LAYOUT, R.layout.heading_surround);
                value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.heading});
                values.add(value);
            }

            if (unmatched1Name != null && unmatched1Name.size() > 0) {
                value = new StringMap();
                value.put(FlexibleListAdapter.TEXT, new String[]{account1Name, "(" + unmatched1Name.size() + ")"});
                value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_none_2);
                value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.type, R.id.value});
                value.put(FlexibleListAdapter.LISTITEM, un1);
                values.add(value);
            }

            if (unmatched2Name != null && unmatched2Name.size() > 0) {
                value = new StringMap();
                value.put(FlexibleListAdapter.TEXT, new String[]{account2Name, "(" + unmatched2Name.size() + ")"});
                value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_none_2);
                value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.type, R.id.value});
                value.put(FlexibleListAdapter.LISTITEM, un2);
                values.add(value);
            }

            first = true;
            for (String type : Match.MIME_TYPE_LIST) {
                String matchLabel = Match.MATCHEDKEY + type + account1Name + ":" + account2Name;
                HashSet<String> matchSet = (HashSet<String>) prefMatch.getStringSet(matchLabel, null);
                if (matchSet != null && matchSet.size() > 0) {
                    if (first) {
                        value = new StringMap();
                        value.put(FlexibleListAdapter.TEXT, new String[]{PMATCHED});
                        value.put(FlexibleListAdapter.LAYOUT, R.layout.heading_surround);
                        value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.heading});
                        values.add(value);
                    }

                    value = new StringMap();
                    value.put(FlexibleListAdapter.TEXT, new String[]{ContactsHelper.getGroupName(type), "(" + matchSet.size() + ")"});
                    if (first)
                        value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_none_2);
                    else
                        value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_top_2);
                    value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.type, R.id.value});
                    value.put(FlexibleListAdapter.LISTITEM, matchLabel);
                    values.add(value);
                    first = false;
                }
            }

            if (matched1 != null && matched1.size() > 0) {
                value = new StringMap();
                value.put(FlexibleListAdapter.TEXT, new String[]{MATCHED});
                value.put(FlexibleListAdapter.LAYOUT, R.layout.heading_surround);
                value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.heading});
                values.add(value);

                value = new StringMap();
                value.put(FlexibleListAdapter.TEXT, new String[]{matched1.size() + " matches"});
                value.put(FlexibleListAdapter.LAYOUT, R.layout.list_border_none_1);
                value.put(FlexibleListAdapter.LAYOUTIDS, new int[]{R.id.value});
                value.put(FlexibleListAdapter.LISTITEM, Match.MATCHEDKEY + account1Name + ":" + account2Name);
                values.add(value);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //set actionbar title
        ((MainActivity) getActivity()).setHeading(getString(R.string.app_name));
    }
}
