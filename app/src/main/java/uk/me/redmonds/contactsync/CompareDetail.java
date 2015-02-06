package uk.me.redmonds.contactsync;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.*;
import android.widget.*;
import android.net.Uri;
import android.database.Cursor;
import android.provider.ContactsContract.*;
import java.util.*;
import java.lang.Long;
import android.preference.*;

public class CompareDetail extends Fragment {
    private FragmentActivity main;
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

        main = (FragmentActivity)this.getActivity();

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
        //dup = new StringList(pref, listItem);
        //dupList = dup.getSparseArray();
        
        HashSet<String> dupSet = (HashSet<String>)pref.getStringSet(listItem, null);
        dupList = new HashMap<String, String> ();
        // To get the Iterator use the iterator() operation
        Iterator dupIt = dupSet.iterator();
        while(dupIt.hasNext()) {
            String[] itemArray = ((String)dupIt.next()).split(":");
            dupList.put(itemArray[0], itemArray[1]);
        }

        layout.removeAllViews();

        LinearLayout.LayoutParams params;
        Uri rawContactUri;
        Uri entityUri;
        String account = listItem.substring(Match.DUPKEY.length());
        String contact;
        Cursor c;

        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();
        Map<String, Integer> index = new HashMap<>();
        Map<String, String> item;

        if (dupList.containsKey(name)) {
            String ids[] = dupList.get(name).split(",");
            for (int i = 0; i < ids.length; i++) {
                contact = "account:\t\t" + account + "\n";
                contact += "id:\t\t" + ids[i];

                rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, new Long(ids[i]));
                entityUri = Uri.withAppendedPath(rawContactUri, RawContacts.Entity.CONTENT_DIRECTORY);
                c = main.getContentResolver().query(entityUri,
                        new String[]{RawContacts._ID,
                                RawContacts.Entity.MIMETYPE,
                                RawContacts.Entity.DATA1,
                                RawContacts.Entity.DATA2,
                                RawContacts.Entity.DATA3},
                        null, null, null);
                try {
                    while (c.moveToNext()) {
                        if (!c.isNull(0) && !c.isNull(2)
                                && !c.getString(2).equals("")) {
                            String group = c.getString(1).split("/",2)[1];
                            String child = "";

                            contact += "\n" + c.getString(1).split("/",2)[1];
                            if (!c.isNull(3) && ((c.getString(1).endsWith("email_v2") && c.getInt(3) == 0) || c.getInt(3) != 0)) {
                                contact += "(";
                                if (c.getString(1).endsWith("email_v2")) {
                                    group = "Email";
                                    child += CommonDataKinds.Email.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4)) + ":";
                                    contact += CommonDataKinds.Email.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("phone_v2")) {
                                    group = "Phone";
                                    child += CommonDataKinds.Phone.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4)) + ":";
                                    contact += CommonDataKinds.Phone.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("organization")) {
                                    group = "Organizations";
                                    child += CommonDataKinds.Organization.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4)) + ":";
                                    contact += CommonDataKinds.Organization.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("relation")) {
                                    group = "Relation";
                                    child += CommonDataKinds.Relation.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4)) + ":";
                                    contact += CommonDataKinds.Relation.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("sipaddress")) {
                                    group = "SIP";
                                    child += CommonDataKinds.SipAddress.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4)) + ":";
                                    contact += CommonDataKinds.SipAddress.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("postal-address_v2")) {
                                    group = "Address";
                                    child += CommonDataKinds.StructuredPostal.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4)) + ":";
                                    contact += CommonDataKinds.StructuredPostal.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("contact_event")) {
                                    group = "Events";
                                    switch (c.getInt(3)) {
                                        case CommonDataKinds.Event.TYPE_ANNIVERSARY:
                                            child += "Anniversary" + ":";
                                            contact += "Anniversary";
                                            break;
                                        case CommonDataKinds.Event.TYPE_BIRTHDAY:
                                            child += "Birthday" + ":";
                                            contact += "Birthday";
                                            break;
                                        case CommonDataKinds.Event.TYPE_OTHER:
                                            child += "Other" + ":";
                                            contact += "Other";
                                            break;
                                        default:
                                            child += c.getString(4) + ":";
                                            contact += c.getString(4);
                                    }
                                } else if (c.getString(1).endsWith("website")) {
                                    group = "Websites";
                                    switch (c.getInt(3)) {
                                        case CommonDataKinds.Website.TYPE_BLOG:
                                            child += "Blog" + ":";
                                            contact += "Blog";
                                            break;
                                        case CommonDataKinds.Website.TYPE_FTP:
                                            child += "Ftp" + ":";
                                            contact += "Ftp";
                                            break;
                                        case CommonDataKinds.Website.TYPE_HOME:
                                            child += "Home" + ":";
                                            contact += "Home";
                                            break;
                                        case CommonDataKinds.Website.TYPE_HOMEPAGE:
                                            child += "Homepage" + ":";
                                            contact += "Homepage";
                                            break;
                                        case CommonDataKinds.Website.TYPE_OTHER:
                                            child += "Other" + ":";
                                            contact += "Other";
                                            break;
                                        case CommonDataKinds.Website.TYPE_PROFILE:
                                            child += "Profile" + ":";
                                            contact += "Profile";
                                            break;
                                        case CommonDataKinds.Website.TYPE_WORK:
                                            child += "Work" + ":";
                                            contact += "Work";
                                            break;
                                        default:
                                            child += c.getString(4) + ":";
                                            contact += c.getString(4);
                                    }
                                } else {
                                    group = "Other";
                                    child += c.getInt(3) + ":";
                                    contact += c.getInt(3);
                                }
                                contact += ")";
                            }
                            contact += ":\t\t" + c.getString(2);
                            child += c.getString(2);
                            item = new HashMap<>();
                            item.put(NAME, group);
                            if (!group.equals("name") && !groupData.contains(item)) {
                                index.put(group, groupData.size());
                                groupData.add(item);
                                childData.add(new ArrayList<Map<String, String>>());
                            }
                            //exclude name
                            if (!group.equals("name")) {
                                HashMap<String, String> childMap = new HashMap<>();
                                childMap.put(NAME, child);
                                childData.get(index.get(group)).add(childMap);
                            }
                        }
                    }
                } finally {
                    c.close();
                }

                /*TextView compareDetail = new TextView(main);
                compareDetail.setTag(account + ":" + ids[i]);
                compareDetail.setTextIsSelectable(false);
                compareDetail.setOnTouchListener(ContactTouch);
                //compareDetail.setBackgroundResource(R.drawable.border);
                compareDetail.setPadding(10, 10, 10, 10);
                compareDetail.setText(contact);
                if (layout.getOrientation() == layout.HORIZONTAL)
                    params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
                else
                    params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 5, 5, 0);*/
                // create a new view
                View contactView = LayoutInflater.from(main)
                           .inflate(R.layout.contact, layoutContainer, false);
                //TextView contactInfo = (TextView)contactView.findViewById(R.id.contact_info);
                //contactInfo.setText(contact);

                ExpandableListView contactInfo = (ExpandableListView) contactView.findViewById(R.id.contact_info);

                // Set up our adapter
                SimpleExpandableListAdapter mAdapter = new SimpleExpandableListAdapter(
                        main,
                        groupData,
                        android.R.layout.simple_expandable_list_item_1,
                        new String[] { NAME },
                        new int[] { android.R.id.text1, android.R.id.text2 },
                        childData,
                        R.layout.listdetail,
                        new String[] { NAME },
                        new int[] { android.R.id.text1, android.R.id.text2 }
                );
                contactInfo.setAdapter(mAdapter);

                layout.addView(contactView);
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
                ids.add(String.valueOf(layout.getChildAt(i).getId()));
                accounts.add((String)layout.getChildAt(i).getTag());
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
                    //MainActivity.Merge(name, ids, listItem);
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
