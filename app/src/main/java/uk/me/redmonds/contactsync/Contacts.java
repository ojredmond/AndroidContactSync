package uk.me.redmonds.contactsync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.*;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.widget.Toast;
import android.preference.*;
import android.content.*;

public class Contacts {
    private HashSet<String> list;
    private HashMap<String,String> accounts = new HashMap<>();
    private Activity main;
    private HashSet<String> contactsOld = null;
    private HashMap<String,HashMap<String,HashSet<HashMap<String,String>>>> contacts = new HashMap<>();
    private SharedPreferences pref;
    public static final String[] types = {
        StructuredName.CONTENT_ITEM_TYPE,
        Phone.CONTENT_ITEM_TYPE,
        Email.CONTENT_ITEM_TYPE,
        Photo.CONTENT_ITEM_TYPE,
        Organization.CONTENT_ITEM_TYPE,
        Im.CONTENT_ITEM_TYPE,
        Nickname.CONTENT_ITEM_TYPE,
        Note.CONTENT_ITEM_TYPE,
        StructuredPostal.CONTENT_ITEM_TYPE,
        GroupMembership.CONTENT_ITEM_TYPE,
        Website.CONTENT_ITEM_TYPE,
        Event.CONTENT_ITEM_TYPE,
        Relation.CONTENT_ITEM_TYPE,
        SipAddress.CONTENT_ITEM_TYPE
    };

    Contacts (Activity m, HashSet<String> ids) {
        main = m;
        list = ids;
        pref = main.getPreferences(Context.MODE_PRIVATE);
        createContacts();
   }
    
    Contacts (Activity m, String[] ids) {
        main = m;
        list = new HashSet<>(Arrays.asList(ids));
        pref = main.getPreferences(Context.MODE_PRIVATE);
        createContacts();
    }

    private void createContacts () {
        Cursor c;
        String ids = "";
        
        for (String i : list) {
            contacts.put(i,new HashMap<String,HashSet<HashMap<String,String>>>());
            ids += i + ",";
            Long id = Long.decode(i);
            
            Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, id);
            c = main.getContentResolver().query(rawContactUri,
                    new String[]{RawContacts.ACCOUNT_NAME, RawContacts.ACCOUNT_TYPE},
                    null, null, null);

            try {
                while (c.moveToNext()) {
                    if (!c.isNull(0) && !c.isNull(1)) {
                        accounts.put(i, c.getString(0));
                    }
                }
            } finally {
                c.close();
            }
        }
        if (ids.length() > 0)
            ids = ids.substring(0, ids.length()-1);

        c = main.getContentResolver().query(Data.CONTENT_URI,
            new String[] {
                Data.RAW_CONTACT_ID,
                Data._ID,
                Data.MIMETYPE,
                Data.DATA1,
                Data.DATA2,
                Data.DATA3
            },
            Data.RAW_CONTACT_ID + " IN (" + ids + ")",
            null, null);

        try {
            while (c.moveToNext()) {
                HashMap<String,HashSet<HashMap<String,String>>> contact = contacts.get(c.getString(0));
                if (!c.isNull(1) && !c.isNull(3) && !c.getString(3).equals("")) {
                    if(!contact.containsKey(c.getString(2)))
                        contact.put(c.getString(2), new HashSet<HashMap<String,String>>());
                    HashSet<HashMap<String,String>> field = contact.get(c.getString(2));
                    HashMap<String,String> value = new HashMap<>();
                    value.put("data1", c.getString(3));
                    value.put("label", getTypeLabel(c.getString(2), c.getInt(4), c.getString(5)));
                    value.put("data2", c.getString(4));
                    value.put("data3", c.getString(5));
                    field.add(value);
                }
            }
        } finally {
            c.close();
        }
    }

    public static String getGroupName (String mime) {
        String group;
        switch (mime) {
            case StructuredName.CONTENT_ITEM_TYPE:
                group = "Name";
                break;
            case Phone.CONTENT_ITEM_TYPE:
                group = "Number";
                break;
            case Email.CONTENT_ITEM_TYPE:
                group = "Email";
                break;
            case Photo.CONTENT_ITEM_TYPE:
                group = "Photo";
                break;
            case Organization.CONTENT_ITEM_TYPE:
                group = "Organization";
                break;
            case Im.CONTENT_ITEM_TYPE:
                group = "IM";
                break;
            case Nickname.CONTENT_ITEM_TYPE:
                group = "Nickname";
                break;
            case Note.CONTENT_ITEM_TYPE:
                group = "Note";
                break;
            case StructuredPostal.CONTENT_ITEM_TYPE:
                group = "Address";
                break;
            case GroupMembership.CONTENT_ITEM_TYPE:
                group = "Group";
                break;
            case Website.CONTENT_ITEM_TYPE:
                group = "Sites";
                break;
            case Event.CONTENT_ITEM_TYPE:
                group = "Event";
                break;
            case Relation.CONTENT_ITEM_TYPE:
                group = "Relation";
                break;
            case SipAddress.CONTENT_ITEM_TYPE:
                group = "SIP";
                break;
            default:
                group = "Other";
        }
        
        return group;
    }
    
    private String getTypeLabel (String mime, Integer type, CharSequence label) {
        switch (mime) {
            case StructuredName.CONTENT_ITEM_TYPE:
                label = null;
                break;
            case Phone.CONTENT_ITEM_TYPE:
                label = Phone.getTypeLabel(main.getResources(), type, label);
                break;
            case Email.CONTENT_ITEM_TYPE:
                label = Email.getTypeLabel(main.getResources(), type, label);
                break;
            case Organization.CONTENT_ITEM_TYPE:
                label = Organization.getTypeLabel(main.getResources(), type, label);
                break;
            case Im.CONTENT_ITEM_TYPE:
                label = Im.getTypeLabel(main.getResources(), type, label);
                break;
            case Nickname.CONTENT_ITEM_TYPE:
                //label = Nickname.getTypeLabel(main.getResources(), type, label);
                break;
            case StructuredPostal.CONTENT_ITEM_TYPE:
                label = StructuredPostal.getTypeLabel(main.getResources(), type, label);
                break;
            case Website.CONTENT_ITEM_TYPE:
                switch (type) {
                    case CommonDataKinds.Website.TYPE_BLOG:
                        label = "Blog";
                        break;
                    case CommonDataKinds.Website.TYPE_FTP:
                        label = "Ftp";
                        break;
                    case CommonDataKinds.Website.TYPE_HOME:
                        label = "Home";
                        break;
                    case CommonDataKinds.Website.TYPE_HOMEPAGE:
                        label = "Homepage";
                        break;
                    case CommonDataKinds.Website.TYPE_OTHER:
                        label = "Other";
                        break;
                    case CommonDataKinds.Website.TYPE_PROFILE:
                        label = "Profile";
                        break;
                    case CommonDataKinds.Website.TYPE_WORK:
                        label = "Work";
                        break;
                }
                break;
            case Event.CONTENT_ITEM_TYPE:
                switch (type) {
                    case CommonDataKinds.Event.TYPE_ANNIVERSARY:
                        label = "Anniversary";
                        break;
                    case CommonDataKinds.Event.TYPE_BIRTHDAY:
                        label = "Birthday";
                        break;
                    case CommonDataKinds.Event.TYPE_OTHER:
                        label = "Other";
                        break;
                }
                break;
            case Relation.CONTENT_ITEM_TYPE:
                label = Relation.getTypeLabel(main.getResources(), type, label);
                break;
            case SipAddress.CONTENT_ITEM_TYPE:
                label = SipAddress.getTypeLabel(main.getResources(), type, label);
                break;
        }
        
        return (String)label;
    }

    public HashMap<String,HashMap<String,HashSet<HashMap<String,String>>>> getContacts() {
        return contacts;
    }

    public String getAccountName (String id) {
        return accounts.get(id);
    }

    public Boolean deleteContacts () {
        String where;
        String[] params;
        ArrayList<ContentProviderOperation> ops = null;

        for (String id : list) {
            where = RawContacts._ID + " = ?";
            params = new String[] {id};

            ops = new ArrayList<>();
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

    public HashMap<String,HashSet<HashMap<String,String>>> mergeContact () {
        HashMap<String,HashSet<HashMap<String,String>>> contact = new HashMap<>();
        for(String type: types) {
            HashSet<HashMap<String,String>> values = new HashSet<>();
            for(String id: contacts.keySet())
                if(contacts.get(id).get(type) != null 
                    && contacts.get(id).get(type).size() > 0)
                    values.addAll(contacts.get(id).get(type));

            if(values.size() > 0)
                contact.put(type, values);
        }
        
        return contact;
    }

    public Boolean saveMergedContact (ArrayList<String[]> contactItems) {
        ArrayList<ContentProviderOperation> ops;
        ContentProviderOperation.Builder opBuilder;
        String type;
        String value;
        String where;
        String origValue;

        if (contactsOld == null) {
            // send error report
            TopExceptionHandler.sendReport (main, TopExceptionHandler.generateReport(new Exception("Contacts Variable not created")));
            return false;
        }

        if (accounts.size() == 0) {
            return false;
        }

        for (String id : list) {
            ops = new ArrayList<> ();
            for (String[] item : contactItems) {
                opBuilder = null;
                where = null;
                type = item[0];
                origValue = item[1];
                value = item[2];

                if (contactsOld.contains(id + "/" + type)
                        && !origValue.equals(value)) {
                    contactsOld.remove(id + "/" + type);
                    opBuilder = ContentProviderOperation.newUpdate(Data.CONTENT_URI);
                    where = Data.RAW_CONTACT_ID + " = " + id;
                } else if (!contactsOld.contains(id + "/" + type)) {
                    opBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                            .withValue(Data.RAW_CONTACT_ID, id);
                } else {
                    contactsOld.remove(id + "/" + type);
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
            for (String s : contactsOld) {
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
                addToUnmatched(id, "", accounts.get(id));
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
