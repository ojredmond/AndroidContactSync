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

        LinearLayout.LayoutParams params;
        Uri rawContactUri;
        Uri entityUri;
        String account = listItem.substring(Match.DUPKEY.length());
        String contactDetail;
        Cursor c;

        View child;
        View groupHeader;
        Map<String, String> item;

        if (dupList.containsKey(name)) {
            String ids[] = dupList.get(name).split(",");
            for (int i = 0; i < ids.length; i++) {
                // create a new view for the contact
                View contactView = LayoutInflater.from(main)
                        .inflate(R.layout.contact, layoutContainer, false);

                contactView.setTag(account + ":" + ids[i]);
                List<Map<String, String>> groupData = new ArrayList<>();
                List<List<View>> childData = new ArrayList<>();
                Map<String, Integer> index = new HashMap<>();
                item = new HashMap<>();
                item.put(NAME, "account");
                groupData.add(item);
                childData.add(new ArrayList<View>());
                groupHeader = LayoutInflater.from(main)
                        .inflate(R.layout.list_row_2, layoutContainer, false);
                ((TextView)groupHeader.findViewById(R.id.type)).setText("Account");
                ((TextView)groupHeader.findViewById(R.id.value)).setText(account);
                ((LinearLayout)groupHeader.findViewById(R.id.row)).setBackgroundColor(getResources().getColor(R.color.nav_background));
                childData.get(0).add(groupHeader);

                groupHeader = LayoutInflater.from(main)
                        .inflate(R.layout.list_row_2, layoutContainer, false);
                ((TextView)groupHeader.findViewById(R.id.type)).setText("ID");
                ((TextView)groupHeader.findViewById(R.id.value)).setText(ids[i]);
                childData.get(0).add(groupHeader);

                rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, Long.valueOf(ids[i]));
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
                            contactDetail = "";

                            if (!c.isNull(3) && ((c.getString(1).endsWith("email_v2") && c.getInt(3) == 0) || c.getInt(3) != 0)) {
                                if (c.getString(1).endsWith("email_v2")) {
                                    group = "Email";
                                    contactDetail += CommonDataKinds.Email.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("phone_v2")) {
                                    group = "Phone";
                                    contactDetail += CommonDataKinds.Phone.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("organization")) {
                                    group = "Organizations";
                                    contactDetail += CommonDataKinds.Organization.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("relation")) {
                                    group = "Relation";
                                    contactDetail += CommonDataKinds.Relation.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("sipaddress")) {
                                    group = "SIP";
                                    contactDetail += CommonDataKinds.SipAddress.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("postal-address_v2")) {
                                    group = "Address";
                                    contactDetail += CommonDataKinds.StructuredPostal.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("contact_event")) {
                                    group = "Events";
                                    switch (c.getInt(3)) {
                                        case CommonDataKinds.Event.TYPE_ANNIVERSARY:
                                            contactDetail += "Anniversary";
                                            break;
                                        case CommonDataKinds.Event.TYPE_BIRTHDAY:
                                            contactDetail += "Birthday";
                                            break;
                                        case CommonDataKinds.Event.TYPE_OTHER:
                                            contactDetail += "Other";
                                            break;
                                        default:
                                            contactDetail += c.getString(4);
                                    }
                                } else if (c.getString(1).endsWith("website")) {
                                    group = "Websites";
                                    switch (c.getInt(3)) {
                                        case CommonDataKinds.Website.TYPE_BLOG:
                                            contactDetail += "Blog";
                                            break;
                                        case CommonDataKinds.Website.TYPE_FTP:
                                            contactDetail += "Ftp";
                                            break;
                                        case CommonDataKinds.Website.TYPE_HOME:
                                            contactDetail += "Home";
                                            break;
                                        case CommonDataKinds.Website.TYPE_HOMEPAGE:
                                            contactDetail += "Homepage";
                                            break;
                                        case CommonDataKinds.Website.TYPE_OTHER:
                                            contactDetail += "Other";
                                            break;
                                        case CommonDataKinds.Website.TYPE_PROFILE:
                                            contactDetail += "Profile";
                                            break;
                                        case CommonDataKinds.Website.TYPE_WORK:
                                            contactDetail += "Work";
                                            break;
                                        default:
                                            contactDetail += c.getString(4);
                                    }
                                } else {
                                    group = "Other";
                                    contactDetail += c.getInt(3);
                                }
                                child = LayoutInflater.from(main)
                                        .inflate(R.layout.list_row_2, layoutContainer, false);
                                ((TextView)child.findViewById(R.id.type)).setText(contactDetail);
                            } else {
                                child = LayoutInflater.from(main)
                                        .inflate(R.layout.list_row_1, layoutContainer, false);
                            }

                            ((TextView)child.findViewById(R.id.value)).setText(c.getString(2));

                            item = new HashMap<>();
                            item.put(NAME, group);
                            if (!group.equals("name") && !groupData.contains(item)) {
                                index.put(group, groupData.size());
                                groupData.add(item);
                                childData.add(new ArrayList<View>());
                                groupHeader = LayoutInflater.from(main)
                                        .inflate(R.layout.list_heading, layoutContainer, false);
                                ((TextView)groupHeader.findViewById(R.id.heading)).setText(group);
                                childData.get(index.get(group)).add(groupHeader);
                            }
                            //exclude name
                            if (!group.equals("name")) {
                                childData.get(index.get(group)).add(child);
                            }
                        }
                    }
                } finally {
                    c.close();
                }

                LinearLayout contactInfo = (LinearLayout) contactView.findViewById(R.id.contact_info);

                for (List<View> l: childData)
                    for (View childItem: l)
                        contactInfo.addView(childItem);

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
