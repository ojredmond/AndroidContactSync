package uk.me.redmonds.contactsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import android.widget.*;
import android.widget.AdapterView.*;

public class MatchContact extends Fragment 
        implements OnClickListener, OnItemClickListener {
    private final static String NAME = "Name";
    private final static String DESCRIPTION = "Desc";
    private MainActivity main;
    private String id;
    private HashMap<String, String> unmatchedList;
    private HashMap<String, String> matchedList;
    private String listItem;
    private String name;

    @Override
    public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
    {
        Toast.makeText(main,""+((View)p2.getParent()).getId()+" " +R.id.unmatched_list,Toast.LENGTH_SHORT).show();
        String linkName = (String) ((TextView) p2).getText();
        ArrayList<String> ids = new ArrayList<>();
        ids.add(id);
        //unmatched list
        if (((View)p2.getParent()).getId() == R.id.unmatched_list) {
            ids.add(unmatchedList.get(linkName));
            main.Merge(name, ids.toArray(new String[ids.size()]), listItem);
            //matched list
        } else if (((View)p2.getParent()).getId() == R.id.matched_list) {
            Collections.addAll(ids, matchedList.get(linkName).split(":"));
            main.Merge(name, ids.toArray(new String[ids.size()]), listItem);
        }
    }

    @Override
    public void onClick(View p1)
    {
        View unmatchedList;
        View matchedList;
        
        switch (p1.getId()) {
            case R.id.delete_contact:
                HashSet<String> list = new HashSet<>();
                list.add(String.valueOf(id));
                ContactsHelper contacts = new ContactsHelper(main, listItem, name, list);
                contacts.deleteContacts();
                break;
            case R.id.unmatched_group:
                unmatchedList = ((ViewGroup)p1.getParent()).findViewById(R.id.unmatched_list);
                matchedList = ((ViewGroup)p1.getParent()).findViewById(R.id.matched_list);
                
                if(unmatchedList.getVisibility() == View.GONE) {
                    matchedList.setVisibility(View.GONE);
                    unmatchedList.setVisibility(View.VISIBLE);
                } else {
                    unmatchedList.setVisibility(View.GONE);
                    matchedList.setVisibility(View.GONE);
                }
                break;
            case R.id.matched_group:
                unmatchedList = ((ViewGroup)p1.getParent()).findViewById(R.id.unmatched_list);
                matchedList = ((ViewGroup)p1.getParent()).findViewById(R.id.matched_list);
    
                if(matchedList.getVisibility() == View.GONE) {
                    unmatchedList.setVisibility(View.GONE);
                    matchedList.setVisibility(View.VISIBLE);
                } else {
                    unmatchedList.setVisibility(View.GONE);
                    matchedList.setVisibility(View.GONE);
                }
                break;
        }
    }

    public boolean onChildClick(ExpandableListView p1, View p2, int p3, int p4, long p5) {
        //unmatched list
        if (p3 == 0) {
            String linkName = (String) ((TextView) p2).getText();
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            ids.add(unmatchedList.get(linkName));
            main.Merge(name, ids.toArray(new String[ids.size()]), listItem);
            //matched list
        } else if (p3 == 1) {
            String linkName = (String) ((TextView) p2).getText();
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            Collections.addAll(ids, matchedList.get(linkName).split(":"));
            main.Merge(name, ids.toArray(new String[ids.size()]), listItem);
        }

        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        main = (MainActivity) getActivity();

        Bundle args = getArguments();
        listItem = args.getString("listItem");
        name = args.getString("name");
        id = args.getString("ids");
        String accountSelected = listItem.split(":")[1];
        String accountOther = listItem.split(":")[2];

        View view = inflater.inflate(R.layout.fragment_unmatched, container, false);
        Button btn = (Button) view.findViewById(R.id.delete_contact);
        btn.setOnClickListener(this);

        SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
        HashSet<String> um = (HashSet<String>) pref.getStringSet(Match.UNMATCHNAMEKEY + accountOther + ":" + accountSelected, null);
        unmatchedList = new HashMap<>();
        ArrayList<String> unmatchedItems = new ArrayList<>();

        for (String anUm : um) {
            String[] itemArray = (anUm).split(":");
            unmatchedList.put(itemArray[0], itemArray[1]);
            unmatchedItems.add(itemArray[0]);
        }
        Collections.sort(unmatchedItems);

        //add unmatched heading
        ((TextView)view.findViewById(R.id.unmatched_group).findViewById(R.id.type)).setText("Unmatched");
        ((TextView)view.findViewById(R.id.unmatched_group).findViewById(R.id.value)).setText("("+unmatchedItems.size()+")");
        ((ListView)view.findViewById(R.id.unmatched_list)).setAdapter(new ArrayAdapter<String>(
                                                                            main,
                                                                            R.layout.list_row_1,
                                                                            R.id.value,
                                                                            unmatchedItems));
        view.findViewById(R.id.unmatched_group).setOnClickListener(this);            
        ((ListView)view.findViewById(R.id.unmatched_list)).setOnItemClickListener(this);            

        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
        HashSet<String> accountSet = (HashSet<String>) pref.getStringSet(Match.ACCOUNTKEY + accountSelected, null);
        HashMap<String, String> account = new HashMap<>();
        for (String entry : accountSet) {
            String contact[] = entry.split(":");
            account.put(contact[0], contact[1]);
        }

        HashSet<String> md = (HashSet<String>) pref.getStringSet(Match.MATCHEDKEY + accountSelected + ":" + accountOther, null);
        matchedList = new HashMap<>();
        ArrayList<String> matchedItems = new ArrayList<>();

        for (String item : md) {
            String[] itemArray = (item).split(":");
            String itemName = account.get(itemArray[0]);
            matchedList.put(itemName, item);
            matchedItems.add(itemName);
        }
        Collections.sort(matchedItems);

        //add matched heading
        ((TextView)view.findViewById(R.id.matched_group).findViewById(R.id.type)).setText("Matched");
        ((TextView)view.findViewById(R.id.matched_group).findViewById(R.id.value)).setText("("+matchedItems.size()+")");
        ((ListView)view.findViewById(R.id.matched_list)).setAdapter(new ArrayAdapter<String>(
                                                                            main,
                                                                            R.layout.list_row_1,
                                                                            R.id.value,
                                                                            matchedItems));
        view.findViewById(R.id.matched_group).setOnClickListener(this);
        ((ListView)view.findViewById(R.id.matched_list)).setOnItemClickListener(this);

        return view;
    }
}
