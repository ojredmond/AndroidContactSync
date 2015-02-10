package uk.me.redmonds.contactsync;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.*;
import android.net.Uri;
import android.database.Cursor;
import android.provider.ContactsContract.*;
import java.util.*;
import java.lang.Long;

public class CompareDetail extends Fragment {
    private MainActivity main;
    private HashSet<String> selected;
    private String name;
    private LinearLayout layout;
    private HashMap<String,String> dupList;
    //private StringList dup;
    private String listItem;
    //private SparseArray<String> dupList;
    private SharedPreferences pref;
    private ViewGroup layoutContainer;
    private View compareView;
    private final static String NAME = "Name";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutContainer = container;

        compareView = inflater.inflate(R.layout.compare, container, false);

        // add listener to buttons
        Button bDel = (Button)compareView.findViewById(R.id.delete_contact);
        Button bMerge = (Button)compareView.findViewById(R.id.merge_contact);
        Button bUn = (Button)compareView.findViewById(R.id.unmatched_contact);
        bDel.setOnClickListener(ButtonClick);
        bMerge.setOnClickListener(ButtonClick);
        bUn.setOnClickListener(ButtonClick);

        main = (MainActivity)this.getActivity();
        main.onSectionAttached(getString(R.string.title_activity_dup));

        Bundle args = getArguments();
        if (args == null) {
            return compareView;
        }
        listItem = args.getString("listItem");
        name = args.getString("name");

        pref = main.getPreferences(Context.MODE_PRIVATE);
        selected = (HashSet<String>)pref.getStringSet("selected-" + listItem, null);

        layout = (LinearLayout)compareView.findViewById(R.id.compare);
        fillLayout();

        return compareView;
    }

    private Boolean fillLayout () {
        HashSet<String> dupSet = (HashSet<String>)pref.getStringSet(listItem, null);
        dupList = new HashMap<> ();
        // To get the Iterator use the iterator() operation
        Iterator dupIt = dupSet.iterator();
        while(dupIt.hasNext()) {
            String[] itemArray = ((String)dupIt.next()).split(":");
            dupList.put(itemArray[0], itemArray[1]);
        }

        layout.removeAllViews();

        String account = listItem.substring(Match.DUPKEY.length());

        if (dupList.containsKey(name)) {
            String ids[] = dupList.get(name).split(",");
            Contacts cObj = new Contacts(main, ids);
            HashMap<String,HashMap<String,HashSet<HashMap<String,String>>>> contacts = cObj.getContacts();
            for (String id: ids) {
                // create a new view for the contact
                View contactView = LayoutInflater.from(main)
                        .inflate(R.layout.contact, layoutContainer, false);
                contactView.setTag(account + ":" + id);
                layout.addView(contactView);
                LinearLayout contactInfo = (LinearLayout) contactView.findViewById(R.id.contact_info);
                View accountInfo = LayoutInflater.from(main)
                        .inflate(R.layout.list_row_2, layoutContainer, false);
                ((TextView)accountInfo.findViewById(R.id.type)).setText("Account");
                ((TextView)accountInfo.findViewById(R.id.value)).setText(account);
                ((LinearLayout)accountInfo.findViewById(R.id.row)).setBackgroundColor(getResources().getColor(R.color.nav_background));
                contactInfo.addView(accountInfo);

                HashMap<String,HashSet<HashMap<String,String>>> contact = contacts.get(id);
                for(String type: Contacts.types) {
                    if(contact.get(type) != null 
                        && contact.get(type).size() > 0) {
                        TextView contactHeading = (TextView)LayoutInflater.from(main)
                            .inflate(R.layout.list_heading, layoutContainer, false);
                        contactHeading.setText(Contacts.getGroupName(type));
                        contactInfo.addView(contactHeading);
                        for(HashMap<String,String> item: contact.get(type)) {
                            if(item.get("label") == null) {
                                TextView contactValue = (TextView)LayoutInflater.from(main)
                                    .inflate(R.layout.list_row_1, layoutContainer, false);
                                contactValue.setText(item.get("data1"));
                                contactInfo.addView(contactValue);
                            } else {
                                LinearLayout rowLayout = (LinearLayout)LayoutInflater.from(main)
                                    .inflate(R.layout.list_row_2, layoutContainer, false);
                                ((TextView)rowLayout.findViewById(R.id.value)).setText(item.get("data1"));
                                ((TextView)rowLayout.findViewById(R.id.type)).setText(item.get("label"));
                                contactInfo.addView(rowLayout);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    OnTouchListener ContactTouch = new OnTouchListener() {

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                return false;
            }

            String id = String.valueOf(v.getId());
            if (selected != null && selected.contains(id)) {
                selected.remove(id);
                //v.setBackgroundResource(R.drawable.border);
            } else {
                if (selected == null) {
                    selected = new HashSet<String>();
                }
                selected.add(id);
                //v.setBackgroundResource(R.drawable.borderhighlight);
            }

            return false;
        }
    };

    private OnClickListener ButtonClick = new OnClickListener() {

        public void onClick(View p1)
        {
            Contacts contacts;
            ArrayList<String> ids = new ArrayList<String>();
            ArrayList<String> accounts = new ArrayList<String>();
            for (int i=0; i < layout.getChildCount(); i++) {
                String contact[] = ((String)layout.getChildAt(i).getTag()).split(":");
                ids.add(contact[1]);
                accounts.add(contact[0]);
            }

            switch (p1.getId()) {
                case R.id.delete_contact:
                    if (selected == null) {
                        //add code for none selected
                        Toast.makeText(main, "No contact selected for " + name, Toast.LENGTH_LONG).show();
                        return;
                    }

                    contacts = new Contacts(main, selected);
                    contacts.deleteContacts();

                    //remove duplicate
                    dupList.remove(name);

                    if ((ids.size() - selected.size()) == 1) {
                        for (int i=0; i < ids.size(); i++) {
                            if (!selected.contains(ids.get(i))) {
                                contacts.addToUnmatched(ids.get(i), name, accounts.get(i));
                            }
                        }
                    }

                    fillLayout();
                    break;
                case R.id.merge_contact:
                    main.Merge(name, ids, listItem);
                    break;
                case R.id.unmatched_contact:
                    dupList.remove(name);
                    for (int i=0; i < ids.size(); i++) {
                        contacts = new Contacts(main, new HashSet<String>(ids));
                        contacts.addToUnmatched(ids.get(i), name, accounts.get(i));
                    }

                    break;
                default:
                    Toast.makeText(main, p1.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };
}
