package uk.me.redmonds.contactsync;

import android.content.*;
import android.os.*;
import android.support.v4.app.*;
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
    private String id;
    private HashMap<String,String> unmatchedList;
    //private SparseArray<String> matchedList;
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
            Toast.makeText(main, "click p3 = 1 " + p4, Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    public void onClick(View p1)
    {
        if (p1.getId() == R.id.delete_contact) {
            /*StringList um = new StringList(pref, listItem);
            HashSet<String> list = new HashSet<String>();
            list.add(String.valueOf(id));
            Contacts contacts = new Contacts(main, list, um);
            contacts.deleteContacts();*/
            Toast.makeText(main, "Deleted " + p1.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        main = (MainActivity) getActivity();

        Bundle args = getArguments();
        listItem = args.getString("listItem");
        name = args.getString("name");
        id = args.getString("id");
        String accountSelected = listItem.split(":")[1];
        String accountOther = listItem.split(":")[2];

        View view = inflater.inflate(R.layout.unmatched, container, false);
        ImageButton btn = (ImageButton) view.findViewById(R.id.delete_contact);
        btn.setOnClickListener(this);

        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.list);

        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();
        List<Map<String, String>> children = new ArrayList<>();
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

        HashSet<String> md = (HashSet<String>)pref.getStringSet(Match.MATCHEDKEY + accountSelected + ":" + accountOther, null);
        HashMap<String,String> matchedList = new HashMap<String, String> ();
        children = new ArrayList<Map<String, String>>();
        // To get the Iterator use the iterator() operation
        Iterator mdIt = md.iterator();
        while(mdIt.hasNext()) {
            String[] itemArray = ((String)mdIt.next()).split(":");
            matchedList.put(itemArray[0], itemArray[1]);
            contactMap = new HashMap<String, String> ();
            contactMap.put(NAME, itemArray[0]);
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
