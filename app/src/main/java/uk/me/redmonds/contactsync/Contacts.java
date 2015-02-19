package uk.me.redmonds.contactsync;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Contacts {
    public static final String TYPE_NAME = StructuredName.CONTENT_ITEM_TYPE;
    public static final String[] TYPES = {
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
    private static final String[] CONTACT_FIELDS = {
            Data.DATA1,
            Data.DATA2,
            Data.DATA3,
            Data.DATA4,
            Data.DATA5,
            Data.DATA6,
            Data.DATA7,
            Data.DATA8,
            Data.DATA9,
            Data.DATA10,
            Data.DATA11,
            Data.DATA12,
            Data.DATA13,
            Data.DATA14,
            Data.DATA15
    };
    public static Boolean groupInc;
    public static Boolean photoInc;
    private static SharedPreferences pref;
    private static String account1Name;
    private static String account2Name;
    private static HashSet<String> account1;
    private static HashSet<String> account2;
    private String listName;
    private HashSet<String> list;
    private HashMap<String, String> accounts = new HashMap<>();
    private HashMap<String, String> listMap = new HashMap<>();
    private MainActivity main;
    private HashMap<String, HashMap<String, HashSet<HashMap<String, String>>>> contacts = new HashMap<>();

    Contacts(Activity m, String l, HashSet<String> ids) {
        main = (MainActivity)m;
        listName = l;
        list = ids;
        pref = main.getPreferences(Context.MODE_PRIVATE);
        createContacts();
   }

    Contacts(Activity m, String l, String[] ids) {
        main = (MainActivity)m;
        listName = l;
        list = new HashSet<>(Arrays.asList(ids));
        pref = main.getPreferences(Context.MODE_PRIVATE);
        createContacts();
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

    private void createContacts() {
        Cursor c;
        String ids = "";

        for (String i : list) {
            contacts.put(i, new HashMap<String, HashSet<HashMap<String, String>>>());
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
            ids = ids.substring(0, ids.length() - 1);

        ArrayList<String> selection = new ArrayList<>();
        selection.add(Data.RAW_CONTACT_ID);
        selection.add(Data._ID);
        selection.add(Data.MIMETYPE);
        selection.addAll(Arrays.asList(CONTACT_FIELDS));

        c = main.getContentResolver().query(Data.CONTENT_URI,
                selection.toArray(new String[selection.size()]),
                Data.RAW_CONTACT_ID + " IN (" + ids + ")",
                null, null);

        try {
            while (c.moveToNext()) {
                HashMap<String, HashSet<HashMap<String, String>>> contact = contacts.get(c.getString(0));
                if (!c.isNull(1) && !c.isNull(3) && !c.getString(3).equals("")) {
                    if (!contact.containsKey(c.getString(2)))
                        contact.put(c.getString(2), new HashSet<HashMap<String, String>>());
                    HashSet<HashMap<String, String>> field = contact.get(c.getString(2));
                    HashMap<String, String> value = new HashMap<>();
                    value.put("value", c.getString(3));
                    value.put("label", getTypeLabel(c.getString(2), c.getInt(4), c.getString(5)));
                    for (int i = 0; i < CONTACT_FIELDS.length; i++)
                        value.put(CONTACT_FIELDS[i], c.getString(i + 3));
                    field.add(value);
                }
            }
        } finally {
            c.close();
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
        account1Name = settings.getString(MainActivity.ACCOUNT1, null);
        account2Name = settings.getString(MainActivity.ACCOUNT2, null);
        groupInc = settings.getBoolean(MainActivity.GROUPS, false);
        photoInc = settings.getBoolean(MainActivity.PHOTOS, false);

        account1 = new HashSet<>();
        account2 = new HashSet<>();

        for (Map.Entry<String, String> e : accounts.entrySet()) {
            if (e.getValue().equals(account1Name))
                account1.add(e.getKey());
            else
                account2.add(e.getKey());
        }
        
        HashSet<String> set = (HashSet<String>) pref.getStringSet(listRef, null);
        for(String item: set) {
            listMap.put(item.split(":")[0],item.split(":")[1]);
        }
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

    public Integer size() {
        return contacts.size();
    }

    public Boolean deleteContacts () {
        return deleteContacts(list);
    }

    public Boolean deleteContacts (HashSet<String> delList) {
        String where;
        String[] params;
        ArrayList<ContentProviderOperation> ops = null;

        for (String id : delList) {
            where = RawContacts._ID + " = ?";
            params = new String[] {id};

            ops = new ArrayList<>();
            ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
                    .withSelection(where, params)
                    .build());

            String name = contacts.get(id).get(TYPE_NAME).iterator().next().get("value");
            contacts.remove(id);
            list.remove(id);
            if (listName.startsWith(Match.MATCHEDKEY)) {
                if (accounts.get(id).equals(account1Name)) {
                    String id1 = id;
                    String id2 = listMap.get(id);
                } else {
                    String id1 = listMap.get(id);
                    String id2 = id;
                }
                removeEntry(Match.MATCHEDKEY + account1Name + ":" + account2Name, id1, id2);
                removeEntry(Match.MATCHEDKEY + account2Name + ":" + account1Name, id2, id1);
            } else
                removeEntry(listName, id, name);
            if (!listName.startsWith(Match.DUPKEY)) {
                String accountList = Match.ACCOUNTKEY + getAccountName(id);
                removeEntry(accountList, id, name);
            }
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
        for(String type: TYPES) {
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

    public Boolean saveMergedContact (HashMap<String,HashSet<HashMap<String,String>>> mergedContact) {
        ArrayList<ContentProviderOperation> ops;
        ContentProviderOperation.Builder opBuilder;
        //String value;
        String where;
        //String origValue;
        HashSet<String> accountsUsed = new HashSet<>();

        if (accounts.size() == 0) {
            return false;
        }

        for (String id : list) {
            if(accountsUsed.contains(accounts.get(id))) {
                HashSet<String> deList = new HashSet<>();
                deList.add(id);
                deleteContacts(deList);
            } else {
                accountsUsed.add(accounts.get(id));
                ops = new ArrayList<>();
                for (String type: TYPES) {
                    //exclude types based on settings
                    if((!type.equals(TYPES[10]) || groupInc)
                       && (!type.equals(TYPES[4]) || photoInc)) {
                        HashSet<HashMap<String, String>> dels;
                        HashSet<HashMap<String, String>> adds;

                        if (mergedContact.get(type) != null)
                            adds = new HashSet<>(mergedContact.get(type));
                        else
                            adds = new HashSet<>();

                        if (contacts.get(id).get(type) != null) {
                            adds.removeAll(contacts.get(id).get(type));
                            dels = new HashSet<>(contacts.get(id).get(type));
                            if (mergedContact.get(type) != null)
                                dels.removeAll(mergedContact.get(type));
                        } else {
                            dels = new HashSet<>();
                        }
                        for (HashMap<String, String> item : dels) {
                            opBuilder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
							ArrayList<String> selection = new ArrayList<>();
							where = "? = ?";
							selection.add(Data.RAW_CONTACT_ID);
							selection.add(id);
							
							where += " AND ? = ?";
							selection.add(Data.MIMETYPE);
							selection.add(type);
                            
							for(String field: CONTACT_FIELDS) {
								if(item.get(field) != null) {
									where += " AND ? = ?";
									selection.add(field);
									selection.add(item.get(field));
								}
							}

                            opBuilder.withSelection(where, selection.toArray(new String[selection.size()]));
                            ops.add(opBuilder.build());
                        }
                        for (HashMap<String, String> item : adds) {
                            opBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                    .withValue(Data.RAW_CONTACT_ID, id)
                                    .withValue(Data.MIMETYPE, type);
							for(String field: CONTACT_FIELDS) {
								if(item.get(field) != null)
									opBuilder.withValue(field, item.get(field));
							}

                            ops.add(opBuilder.build());
                        }
                    }
                }

                if (ops.size() > 0) {
                    try {
                        main.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    } catch (Throwable e) {
                        // send error report
                        TopExceptionHandler.sendReport(main, TopExceptionHandler.generateReport(e));
                        return false;
                    }
                }
            }
        }

        if (list.size() == 1)
            addToUnmatched();
        else
            addToMatched();

        return true;
    }

    public void addToUnmatched () {
        String uName = null;
        String accountKey = null;

        for (String id: list) {
            if (accounts.get(id).equals(account1Name)) {
                uName = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
                accountKey = Match.ACCOUNTKEY + account1Name;
            } else if (accounts.get(id).equals(account2Name)) {
                uName = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;
                accountKey = Match.ACCOUNTKEY + account2Name;
            }

            String name = contacts.get(id).get(TYPE_NAME).iterator().next().get("value");
            addEntry(uName, name + ":" + id);
            if (listName.startsWith(Match.DUPKEY)) {
                addEntry(accountKey, name + ":" + id);
                removeEntry(listName, id, name);
            } else if (listName.startsWith(Match.MATCHEDKEY)) {
                if (accounts.get(id).equals(account1Name)) {
                    String id1 = id;
                    String id2 = listMap.get(id);
                } else {
                    String id1 = listMap.get(id);
                    String id2 = id;
                }
                removeEntry(Match.MATCHEDKEY + account1Name + ":" + account2Name, id1, id2);
                removeEntry(Match.MATCHEDKEY + account2Name + ":" + account1Name, id2, id1);
            }
        }
    }

    public void addToMatched () {
        String matchedName;
        String uName;
        String name;

        for (String id1: account1) {
            for (String id2: account2) {
                name = contacts.get(id1).get(TYPE_NAME).iterator().next().get("value");
                uName = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
                removeEntry(uName, id1, name);

                name = contacts.get(id2).get(TYPE_NAME).iterator().next().get("value");
                uName = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;
                removeEntry(uName, id2, name);

                matchedName = Match.MATCHEDKEY + account1Name + ":" + account2Name;
                addEntry(matchedName, id1 + ":" + id2);

                matchedName = Match.MATCHEDKEY + account2Name + ":" + account1Name;
                addEntry(matchedName, id2 + ":" + id1);
            }
        }
    }

    /*private ContentProviderOperation.Builder setOpBuilder(ContentProviderOperation.Builder opBuilder, String value, String type) {
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
    }*/

    private Boolean removeEntry(String listRef, String id, String name) {
        HashSet<String> set = (HashSet<String>) pref.getStringSet(listRef, null);
        set.remove(name + ":" + id);
        SharedPreferences.Editor e = pref.edit();
        e.putStringSet(listRef, set);
        return e.commit();
    }

    public Boolean addEntry(String listRef, String entry) {
        HashSet<String> set = (HashSet<String>) pref.getStringSet(listRef, null);
        if (set == null)
            return true;
        set.add(entry);
        SharedPreferences.Editor e = pref.edit();
        e.putStringSet(listRef, set);
        return e.commit();
    }
}
