package uk.me.redmonds.contactsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MatchContact extends Fragment
        implements ExpandableListView.OnChildClickListener
{
    private final static String NAME = "Name";
    private final static String DESCRIPTION = "Desc";
    private MainActivity main;
    private String id;
    private HashMap<String,String> unmatchedList;
    private HashMap<String,String> matchedList;
    private String listItem;
    private String name;
    private SharedPreferences pref;

    public boolean onChildClick(ExpandableListView p1, View p2, int p3, int p4, long p5)
    {
        //unmatched list
        if (p3 == 0) {
            String linkName = (String) ((TextView)p2).getText();
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            ids.add(unmatchedList.get(linkName));
            main.Merge(name,ids,listItem);
        //matched list
        } else if (p3 == 1) {
            String linkName = (String) ((TextView)p2).getText();
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            for(String i: matchedList.get(linkName).split(":"))
                ids.add(i);
            main.Merge(name,ids,listItem);
        }

        return false;
    }

    private OnClickListener ButtonClick = new OnClickListener() {
        public void onClick(View p1)
        {
            if (p1.getId() == R.id.delete_contact) {
                HashSet<String> list = new HashSet<String>();
                list.add(String.valueOf(id));
                Contacts contacts = new Contacts(main, list);
                if (contacts.deleteContacts())
                    contacts.removeEntry(listItem,id,name);
            }
    
        }
    };

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
        btn.setOnClickListener(ButtonClick);

        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.list);

        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();
        List<Map<String, String>> children;
        Map<String, String> contactMap;

        Map<String, String> curGroupMap1 = new HashMap<>();
        groupData.add(curGroupMap1);
        curGroupMap1.put(NAME, "Unmatched");
        curGroupMap1.put(DESCRIPTION, accountOther);

        pref = main.getPreferences(Context.MODE_PRIVATE);
        HashSet<String> um = (HashSet<String>)pref.getStringSet(Match.UNMATCHNAMEKEY + accountOther + ":" + accountSelected, null);
        unmatchedList = new HashMap<String, String> ();
        children = new ArrayList<Map<String, String>>();
        // To get the Iterator use the iterator() operation
        Iterator umIt = um.iterator();
        while(umIt.hasNext()) {
            String[] itemArray = ((String)umIt.next()).split(":");
            unmatchedList.put(itemArray[0], itemArray[1]);
            contactMap = new HashMap<String, String> ();
            contactMap.put(NAME, itemArray[0]);
            children.add(contactMap);
        }
        Collections.sort(children, new ListSort());
        childData.add(children);

        Map<String, String> curGroupMap2 = new HashMap<String, String>();
        groupData.add(curGroupMap2);
        curGroupMap2.put(NAME, "Matched");
        curGroupMap2.put(DESCRIPTION, accountSelected + " " + accountOther);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
        HashSet<String> accountSet = (HashSet<String>)pref.getStringSet(Match.ACCOUNTKEY + accountSelected, null);
        HashMap<String,String> account = new HashMap<>();
        for(String entry: accountSet) {
            String contact[] = entry.split(":");
            account.put(contact[0],contact[1]);
        }

        HashSet<String> md = (HashSet<String>)pref.getStringSet(Match.MATCHEDKEY + accountSelected + ":" + accountOther, null);
        matchedList = new HashMap<String, String> ();
        children = new ArrayList<Map<String, String>>();
        for (String item : md) {
            String[] itemArray = (item).split(":");
            String itemName = account.get(itemArray[0]);
            matchedList.put(itemName, item);
            contactMap = new HashMap<String, String>();
            contactMap.put(NAME, itemName);
            children.add(contactMap);
        }
        childData.add(children);

        // Set up our adapter
        SimpleExpandableListAdapter mAdapter = new SimpleExpandableListAdapter(
                main,
                groupData,
                R.layout.list_heading_2,
                new String[] { NAME, DESCRIPTION },
                new int[] { android.R.id.text1, android.R.id.text2 },
                childData,
                R.layout.list_detail,
                new String[] { NAME },
                new int[] { R.id.value }
        );

        listView.setAdapter(mAdapter);
        listView.setOnChildClickListener(this);

        return view;
    }
}
