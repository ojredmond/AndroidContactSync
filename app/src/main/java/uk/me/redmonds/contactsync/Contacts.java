package uk.me.redmonds.contactsync;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.widget.Toast;
import android.preference.*;
import android.content.*;
import android.util.*;

public class Contacts {
    private HashSet<String> list;
    private Activity main;
    private HashSet<String> contacts = null;
    private SharedPreferences pref;

    Contacts (Activity m, HashSet<String> ids) {
        main = m;
        list = ids;
        pref = main.getPreferences(Context.MODE_PRIVATE);
    }

    Contacts (Activity m, HashSet<String> ids, HashSet<String> c) {
        main = m;
        list = ids;
        contacts = c;
        pref = main.getPreferences(Context.MODE_PRIVATE);
    }

    public HashSet<String> getContacts() {
        return contacts;
    }

    public Boolean deleteContacts () {
        String where;
        String[] params;
        ArrayList<ContentProviderOperation> ops = null;

        for (String id : list) {
            where = RawContacts._ID + " = ?";
            params = new String[] {id};

            ops = new ArrayList<ContentProviderOperation>();
            ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
                    .withSelection(where, params)
                    .build());
        }

        if (ops != null) {
            try {
                main.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                // send error report
                TopExceptionHandler.sendReport (main, TopExceptionHandler.generateReport(e));
                return false;
            }
        }
        return true;
    }

    public HashSet<String> mergeContact () {
        Uri rawContactUri;
        Uri entityUri;

        Cursor c = main.getContentResolver().query(Data.CONTENT_URI,
            new String[] {
                Data.RAW_CONTACT_ID,
                Data._ID,
                Data.MIMETYPE,
                Data.DATA1,
                Data.DATA2,
                Data.DATA3
            },
            Data.RAW_CONTACT_ID + " IN (?)",
            new String[]{ids});

            try {
                while (c.moveToNext()) {
                String rawId = c.getString(0);
                String dataId = c.getString(0);
                    if (!c.isNull(1)) {
                        String mimeType = c.getString(2);
                        String data = c.getString(3);
                        String type = c.getString(4);
                        String type_other = c.getString(5);
                    }
                }
            } finally {
                c.close();
            }

        HashSet<String> contact;
        for (String i : list) {
            contact = getContact("String");
        }
        return contact;
    }

    public HashSet<> getContact (String objectType) {
        HashSet<String> contact = new HashSet<String> ();
        contacts = new HashSet<String> ();
        rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, Long.ValuOf(i));
        entityUri = Uri.withAppendedPath(rawContactUri, RawContacts.Entity.CONTENT_DIRECTORY);
        Cursor c = main.getContentResolver().query(entityUri,
                new String[]{RawContacts.Entity.DATA_ID, RawContacts.Entity.MIMETYPE, RawContacts.Entity.DATA1, RawContacts.Entity.DATA2},
                null, null, null);

        try {
            while (c.moveToNext()) {
                if (!c.isNull(0) && !c.isNull(2)
                        && !c.getString(2).equals("")) {
                    contact.add(c.getString(1).split("/",2)[1]
                            + "/" + c.getString(3)
                            + ";" + c.getString(2)
                            + ";" + c.getString(2));
                    contacts.add(String.valueOf(i) + "/" + c.getString(1).split("/",2)[1]
                            + "/" + c.getString(3)
                            + ";" + c.getString(2));
                }
            }
        } finally {
            c.close();
        }
        
        return contact;
    }

    public Boolean saveMergedContact (ArrayList<String[]> contactItems) {
        Uri rawContactUri;
        Cursor c;
        ArrayList<ContentProviderOperation> ops;
        ContentProviderOperation.Builder opBuilder;
        String type;
        String value;
        String where = null;
        String origValue;
        HashSet<String> accounts = new HashSet<String>();

        if (contacts == null) {
            // send error report
            TopExceptionHandler.sendReport (main, TopExceptionHandler.generateReport(new Exception("Contacts Variable not created")));
            return false;
        }

        for (String i : list) {
            rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, Long.getLong(i));
            c = main.getContentResolver().query(rawContactUri,
                    new String[]{RawContacts.ACCOUNT_NAME, RawContacts.ACCOUNT_TYPE},
                    null, null, null);

            try {
                while (c.moveToNext()) {
                    if (!c.isNull(0) && !c.isNull(1)) {
                        accounts.add(c.getString(0));
                    }
                }
            } finally {
                c.close();
            }
        }

        if (accounts.size() == 0) {
            return false;
        }

        for (String id : new ArrayList<String>(list)) {
            ops = new ArrayList<ContentProviderOperation> ();
            for (String[] item : contactItems) {
                opBuilder = null;
                where = null;
                type = item[0];
                origValue = item[1];
                value = item[2];

                if (contacts.contains(id + "/" + type)
                        && !origValue.equals(value)) {
                    contacts.remove(id + "/" + type);
                    opBuilder = ContentProviderOperation.newUpdate(Data.CONTENT_URI);
                    where = Data.RAW_CONTACT_ID + " = " + id;
                } else if (!contacts.contains(id + "/" + type)) {
                    opBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                            .withValue(Data.RAW_CONTACT_ID, id);
                } else {
                    contacts.remove(id + "/" + type);
                    opBuilder = null;
                }

                if (opBuilder != null) {
                    type = type.split(";",2)[0];
                    if (where != null) {
                        where += " AND Data1 = ?";
                        opBuilder.withSelection(where, new String[] {origValue});
                    }
                    opBuilder = setOpBuilder(opBuilder, value, type);

                    ops.add(opBuilder.build());
                }
            }
            // remove any deleted elements
            for (String s : contacts) {
                if (s.startsWith(id)) {
                    value = s.split(";",2)[1];
                    type = s.split(";",2)[0].split("/",2)[1];
                    opBuilder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
                    where = RawContacts._ID + " = " + id;
                    where += " AND Data1 = ?";
                    opBuilder.withSelection(where, new String[] {value});

                    ops.add(opBuilder.build());
                }
            }

            if (ops.size() > 0) {
                try {
                    main.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (Throwable e) {
                    // send error report
                    TopExceptionHandler.sendReport (main, TopExceptionHandler.generateReport(e));
                    return false;
                }
            }

            //add id to fragment_unmatched
            //remove from list delete other contacts
            if (accounts.size() == 1) {
                //add name
                addToUnmatched(id, "", accounts.iterator().next());
                return deleteContacts();
            }
        }

        return true;
    }

    public void addToUnmatched (String id, String name, String account) {
        list.remove(id);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
        String account1Name = settings.getString("account1", null);
        String account2Name = settings.getString("account2", null);
        String uName = null;

        if (account.equals(account1Name)) {
            uName = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
        } else if (account.equals(account2Name)) {
            uName = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
        }

        addEntry(uName, id, name);
    }


    private ContentProviderOperation.Builder setOpBuilder(ContentProviderOperation.Builder opBuilder, String value, String type) {
        if (type.startsWith("name")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, value);
        } else if (type.startsWith("email_v2")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Email.ADDRESS, value)
                    .withValue(CommonDataKinds.Email.TYPE, type.split("/")[1]);
        } else if (type.startsWith("phone_v2")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Phone.NUMBER, value)
                    .withValue(CommonDataKinds.Phone.TYPE, Integer.parseInt(type.split("/")[1]));
        } else if (type.startsWith("organization")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Organization.COMPANY, value)
                    .withValue(CommonDataKinds.Organization.TYPE, type.split("/")[1]);
        } else if (type.startsWith("relation")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.Relation.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Relation.NAME, value)
                    .withValue(CommonDataKinds.Relation.TYPE, type.split("/")[1]);
        } else if (type.startsWith("sipaddress")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.SipAddress.SIP_ADDRESS, value)
                    .withValue(CommonDataKinds.SipAddress.TYPE, type.split("/")[1]);
        } else if (type.startsWith("postal-address_v2")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, value)
                    .withValue(CommonDataKinds.StructuredPostal.TYPE, type.split("/")[1]);
        } else if (type.startsWith("contact_event")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Event.START_DATE, value)
                    .withValue(CommonDataKinds.Event.TYPE, type.split("/")[1]);
        } else if (type.startsWith("group_membership")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.GroupMembership.GROUP_ROW_ID, value);
        } else if (type.startsWith("website")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Website.URL, value)
                    .withValue(CommonDataKinds.Website.TYPE, type.split("/")[1]);
        } else if (type.startsWith("identity")) {
            opBuilder.withValue(Data.MIMETYPE, CommonDataKinds.Identity.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Identity.IDENTITY, value)
                    .withValue(CommonDataKinds.Identity.NAMESPACE, type.split("/")[1]);
        }
        return opBuilder;
    }

    public Boolean removeEntry (String listName, String id, String name) {
        HashSet set = (HashSet<String>)pref.getStringSet(listName, null);
        set.remove(name + ":" + id);
        SharedPreferences.Editor e = pref.edit();
        e.putStringSet(listName,set);
        return true;
    }

    public Boolean addEntry (String listName, String id, String name) {
        HashSet set = (HashSet<String>)pref.getStringSet(listName, null);
        set.add(name + ":" + id);
        SharedPreferences.Editor e = pref.edit();
        e.putStringSet(listName,set);
        return e.commit();
    }
}
