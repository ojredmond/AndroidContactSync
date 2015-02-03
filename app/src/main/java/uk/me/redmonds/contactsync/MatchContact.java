package uk.me.redmonds.contactsync;

import android.content.*;
import android.os.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.util.*;

public class MatchContact extends Fragment
        implements ExpandableListView.OnChildClickListener, ImageButton.OnClickListener
{
    private final static String NAME = "Name";
    private final static String DESCRIPTION = "Desc";
    private MainActivity main;
    private SparseArray<String> unmatchedList;
    private SparseArray<String> matchedList;
    private int id;
    private String listItem;
    private String name;
    private SharedPreferences pref;

    public boolean onChildClick(ExpandableListView p1, View p2, int p3, int p4, long p5)
    {
        if (p3 == 0) {
            Toast.makeText(main, "click " + unmatchedList.valueAt(p4), Toast.LENGTH_SHORT).show();
        } else if (p3 == 1) {
            Toast.makeText(main, "click " + matchedList.valueAt(p4), Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    public void onClick(View p1)
    {
        if (p1.getId() == R.id.delete_contact) {
            StringList um = new StringList(pref, listItem);
            HashSet<String> list = new HashSet<String>();
            list.add(String.valueOf(id));
            Contacts contacts = new Contacts(main, list, um);
            contacts.deleteContacts();
            Toast.makeText(main, "Deleted " + this.getActivity().getClass(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        main = (MainActivity) this.getActivity();

        Bundle args = getArguments();
        listItem = args.getString("listItem");
        name = args.getString("name");
        id = args.getInt("id");
        String accountSelected = listItem.split(":")[1];
        String accountOther = listItem.split(":")[2];

        View view = inflater.inflate(R.layout.unmatched, container, false);
        ImageButton btn = (ImageButton) view.findViewById(R.id.delete_contact);
        btn.setOnClickListener(this);

        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.list);

        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

        Map<String, String> curGroupMap1 = new HashMap<String, String>();
        groupData.add(curGroupMap1);
        curGroupMap1.put(NAME, "Unmatched");
        curGroupMap1.put(DESCRIPTION, accountOther);

        pref = main.getPreferences(Context.MODE_PRIVATE);
        StringList um = new StringList(pref, Match.UNMATCHNAMEKEY + accountOther + ":" + accountSelected);

        List<Map<String, String>> children = new ArrayList<Map<String, String>>();
        Map<String, String> contactMap;
        unmatchedList = um.getSparseArray();
        for (int i=0; i < unmatchedList.size(); i++) {
            contactMap = new HashMap<String, String> ();
            contactMap.put(NAME, unmatchedList.valueAt(i));
            children.add(contactMap);
        }
        childData.add(children);

        Map<String, String> curGroupMap2 = new HashMap<String, String>();
        groupData.add(curGroupMap2);
        curGroupMap2.put(NAME, "Matched");
        curGroupMap2.put(DESCRIPTION, accountSelected + " " + accountOther);

        StringList md = new StringList(pref, Match.MATCHEDKEY + accountSelected + ":" + accountOther);

        children = new ArrayList<Map<String, String>>();
        matchedList = md.getSparseArray();
        for (int i=0; i < matchedList.size(); i++) {
            contactMap = new HashMap<String, String> ();
            contactMap.put(NAME, matchedList.valueAt(i));
            children.add(contactMap);
        }
        childData.add(children);


        // Set up our adapter
        SimpleExpandableListAdapter mAdapter = new SimpleExpandableListAdapter(
                main,
                groupData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] { NAME, DESCRIPTION },
                new int[] { android.R.id.text1, android.R.id.text2 },
                childData,
                R.layout.listdetail,
                new String[] { NAME },
                new int[] { android.R.id.text1, android.R.id.text2 }
        );

        listView.setAdapter(mAdapter);
        listView.setOnChildClickListener(this);

        return view;
    }
}
