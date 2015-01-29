package uk.me.redmonds.contactsync;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.preference.*;

public class CompareDetail extends Fragment {
    private MainActivity main;
    private HashSet<String> selected;
    private String name;
    private LinearLayout layout;
    private StringList dup;
    private String listItem;
    private SparseArray<String> dupList;
    private SharedPreferences pref;

    private View compareView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        compareView = inflater.inflate(R.layout.compare, container, false);

        // add listener to buttons
        ButtonTouch buttonTouch = new ButtonTouch();
        ImageButton bDel = (ImageButton)compareView.findViewById(R.id.delete_contact);
        ImageButton bMerge = (ImageButton)compareView.findViewById(R.id.merge_contact);
        ImageButton bUn = (ImageButton)compareView.findViewById(R.id.unmatched_contact);
        bDel.setOnTouchListener(buttonTouch);
        bDel.setOnClickListener(ButtonClick);
        bMerge.setOnTouchListener(buttonTouch);
        bMerge.setOnClickListener(ButtonClick);
        bUn.setOnTouchListener(buttonTouch);
        bUn.setOnClickListener(ButtonClick);

        main = (MainActivity)this.getActivity();

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
        dup = new StringList(pref, listItem);
        dupList = dup.getSparseArray();

        layout.removeAllViews();

        String contact = "";
        LinearLayout.LayoutParams params;
        Uri rawContactUri;
        Uri entityUri;
        String account = "";
        Cursor c;
        for (int i=0; i < dupList.size(); i++) {
            if (dupList.valueAt(i).equals(name)) {
                rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, dupList.keyAt(i));
                c = main.getContentResolver().query(rawContactUri,
                        new String[]{RawContacts.ACCOUNT_NAME, RawContacts.ACCOUNT_TYPE},
                        null, null, null);

                try {
                    while (c.moveToNext()) {
                        if (!c.isNull(0) && !c.isNull(1)) {
                            account = c.getString(0);
                            contact = "account:\t\t" + account + "\n";
                        }
                    }
                } finally {
                    c.close();
                }
                contact += "id:\t\t" + dupList.keyAt(i);
                rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, dupList.keyAt(i));
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
                            contact += "\n" + c.getString(1).split("/",2)[1];
                            if (!c.isNull(3) && ((c.getString(1).endsWith("email_v2") && c.getInt(3) == 0) || c.getInt(3) != 0)) {
                                contact += "(";
                                if (c.getString(1).endsWith("email_v2")) {
                                    contact += CommonDataKinds.Email.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("phone_v2")) {
                                    contact += CommonDataKinds.Phone.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("organization")) {
                                    contact += CommonDataKinds.Organization.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("relation")) {
                                    contact += CommonDataKinds.Relation.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("sipaddress")) {
                                    contact += CommonDataKinds.SipAddress.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("postal-address_v2")) {
                                    contact += CommonDataKinds.StructuredPostal.getTypeLabel(main.getResources(), c.getInt(3), c.getString(4));
                                } else if (c.getString(1).endsWith("contact_event")) {
                                    switch (c.getInt(3)) {
                                        case CommonDataKinds.Event.TYPE_ANNIVERSARY:
                                            contact += "Anniversary";
                                            break;
                                        case CommonDataKinds.Event.TYPE_BIRTHDAY:
                                            contact += "Birthday";
                                            break;
                                        case CommonDataKinds.Event.TYPE_OTHER:
                                            contact += "Other";
                                            break;
                                        default:
                                            contact += c.getString(4);
                                    }
                                } else if (c.getString(1).endsWith("website")) {
                                    switch (c.getInt(3)) {
                                        case CommonDataKinds.Website.TYPE_BLOG:
                                            contact += "Blog";
                                            break;
                                        case CommonDataKinds.Website.TYPE_FTP:
                                            contact += "Ftp";
                                            break;
                                        case CommonDataKinds.Website.TYPE_HOME:
                                            contact += "Home";
                                            break;
                                        case CommonDataKinds.Website.TYPE_HOMEPAGE:
                                            contact += "Homepage";
                                            break;
                                        case CommonDataKinds.Website.TYPE_OTHER:
                                            contact += "Other";
                                            break;
                                        case CommonDataKinds.Website.TYPE_PROFILE:
                                            contact += "Profile";
                                            break;
                                        case CommonDataKinds.Website.TYPE_WORK:
                                            contact += "Work";
                                            break;
                                        default:
                                            contact += c.getString(4);
                                    }
                                } else {
                                    contact += c.getInt(3);
                                }
                                contact += ")";
                            }
                            contact += ":\t\t" + c.getString(2);
                        }
                    }
                } finally {
                    c.close();
                }

                TextView compareDetail = new TextView(main);
                compareDetail.setId(dupList.keyAt(i));
                compareDetail.setTag(account);
                compareDetail.setTextIsSelectable(false);
                compareDetail.setOnTouchListener(ContactTouch);
                compareDetail.setBackgroundResource(R.drawable.border);
                compareDetail.setPadding(10, 10, 10, 10);
                compareDetail.setText(contact);
                if (layout.getOrientation() == layout.HORIZONTAL)
                    params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
                else
                    params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 5, 5, 0);
                layout.addView(compareDetail, params);
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
                v.setBackgroundResource(R.drawable.border);
            } else {
                if (selected == null) {
                    selected = new HashSet<String>();
                }
                selected.add(id);
                v.setBackgroundResource(R.drawable.borderhighlight);
            }

            return false;
        }
    };

    OnClickListener ButtonClick = new OnClickListener() {

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
                        return;
                    }

                    contacts = new Contacts(main, selected, dup);
                    contacts.deleteContacts();

                    if ((ids.size() - selected.size()) == 1) {
                        for (int i=0; i < ids.size(); i++) {
                            if (!selected.contains(ids.get(i))) {
                                dup.removeEntry(ids.get(i));
                                contacts.addToUnmatched(ids.get(i),accounts.get(i));
                            }
                        }
                    }

                    fillLayout();
                    break;
                case R.id.merge_contact:
                    main.Merge(name, ids, listItem);
                    break;
                case R.id.unmatched_contact:
                    for (int i=0; i < ids.size(); i++) {
                        contacts = new Contacts(main, new HashSet<String>(ids), dup);
                        dup.removeEntry(ids.get(i));
                        contacts.addToUnmatched(ids.get(i), accounts.get(i));
                    }

                    break;
                default:
                    Toast.makeText(main, p1.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };
}
