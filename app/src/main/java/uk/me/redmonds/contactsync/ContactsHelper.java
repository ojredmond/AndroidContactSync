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
import java.util.Iterator;
import java.util.Map;

class ContactsHelper {
    public static final String TYPE_NAME = StructuredName.CONTENT_ITEM_TYPE;
    public static final String TYPE_PHOTO = Photo.CONTENT_ITEM_TYPE;
    public static final String TYPE_GROUP = GroupMembership.CONTENT_ITEM_TYPE;
    public static final String[] TYPES = {
            Photo.CONTENT_ITEM_TYPE,
            StructuredName.CONTENT_ITEM_TYPE,
            Phone.CONTENT_ITEM_TYPE,
            Email.CONTENT_ITEM_TYPE,
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
    public final static String PHOTO = Data.DATA15;
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
    private final String listName;
    private final String listKey;
    private final HashSet<String> list;
    private final HashMap<String, String> accounts = new HashMap<>();
    private final HashMap<String, String> listMap = new HashMap<>();
    private final MainActivity main;
    private final HashMap<String, HashMap<String, HashSet<StringMap>>> contacts = new HashMap<>();

    ContactsHelper(Activity m, String l, String key, HashSet<String> ids) {
        main = (MainActivity)m;
        listName = l;
        listKey = key;
        list = ids;
        pref = main.getPreferences(Context.MODE_PRIVATE);
        createContacts();
   }

    ContactsHelper(Activity m, String l, String key, String[] ids) {
        main = (MainActivity)m;
        listName = l;
        listKey = key;
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
            contacts.put(i, new HashMap<String, HashSet<StringMap>>());
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

        //get settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
        account1Name = settings.getString(MainActivity.ACCOUNT1, null);
        account2Name = settings.getString(MainActivity.ACCOUNT2, null);
        groupInc = settings.getBoolean(MainActivity.GROUPS, false);
        photoInc = settings.getBoolean(MainActivity.PHOTOS, false);

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
                HashMap<String, HashSet<StringMap>> contact = contacts.get(c.getString(0));
                if (!c.isNull(1) && !c.isNull(2) 
                    && (!c.getString(2).equals(TYPE_GROUP) || groupInc)
                    && (!c.getString(2).equals(TYPE_PHOTO) || photoInc)) {
                    
                    if (!contact.containsKey(c.getString(2)))
                        contact.put(c.getString(2), new HashSet<StringMap>());
                    HashSet<StringMap> field = contact.get(c.getString(2));
                    StringMap value = new StringMap();
                    if (!c.isNull(3) && !c.getString(3).equals(""))
                        value.put("value", c.getString(3));
                    value.put("label", getTypeLabel(c.getString(2), c.getInt(4), c.getString(5)));
                    
                    for (int i = 0; i < CONTACT_FIELDS.length; i++)
                        if(c.getType(i + 3) == Cursor.FIELD_TYPE_BLOB)
                            value.put(CONTACT_FIELDS[i], c.getBlob(i + 3));
                        else
                            value.put(CONTACT_FIELDS[i], c.getString(i + 3));
                    field.add(value);
                }
            }
        } finally {
            c.close();
        }

        account1 = new HashSet<>();
        account2 = new HashSet<>();

        for (Map.Entry<String, String> e : accounts.entrySet()) {
            if (e.getValue().equals(account1Name))
                account1.add(e.getKey());
            else
                account2.add(e.getKey());
        }
        
        HashSet<String> set = (HashSet<String>) pref.getStringSet(listName, null);
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

    public HashMap<String,HashMap<String,HashSet<StringMap>>> getContacts() {
        return contacts;
    }

    public String getAccountName (String id) {
        return accounts.get(id);
    }
    
    public HashSet<byte[]> getPhotos() {
        HashSet<byte[]> photos = new HashSet<>();
        for(HashMap<String,HashSet<StringMap>> contact:contacts.values()) {
            for(StringMap photo:contact.get(TYPE_PHOTO)) {
                photos.add(photo.getByteArray(Data.DATA15));
            }
        }
        return photos;
    }
    
    public byte[] getPhoto(String contactId) {
        if(contacts.get(contactId).get(TYPE_PHOTO) == null)
            return null;
        Iterator it = contacts.get(contactId).get(TYPE_PHOTO).iterator();
        if(it.hasNext())
            return ((StringMap)it.next()).getByteArray(Data.DATA15);
        else
            return null;
    }
    
    public int size() {
        return contacts.size();
    }

    public void deleteContacts() {
        deleteContacts(list);
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
                String id1,id2;
                if (accounts.get(id).equals(account1Name)) {
                    id1 = id;
                    id2 = listMap.get(id);
                } else {
                    id1 = listMap.get(id);
                    id2 = id;
                }
                if(listName.equals(Match.MATCHEDKEY + account1Name + ":" + account2Name)) {
                    removeEntry(Match.MATCHEDKEY + account1Name + ":" + account2Name, id2, id1);
                    removeEntry(Match.MATCHEDKEY + account2Name + ":" + account1Name, id1, id2);
                } else {
                    for(String type: Match.MIME_TYPE_LIST) {
                        removeEntry(Match.MATCHEDKEY + type + account1Name + ":" + account2Name, id2, id1);
                        removeEntry(Match.MATCHEDKEY + type + account2Name + ":" + account1Name, id1, id2);
                    }
                }
            } else if (listName.startsWith(Match.DUPKEY)) {
                for(String type: Match.MIME_TYPE_LIST) {
                    removeEntry(Match.DUPKEY + type + accounts.get(id), id);
                }
            }
            removeEntry(listName, id, name);
            
            String accountList = Match.ACCOUNTKEY + getAccountName(id);
            removeEntry(accountList, id, name);
                
            
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

    public HashMap<String,HashSet<StringMap>> mergeContact () {
        HashMap<String,HashSet<StringMap>> contact = new HashMap<>();
        for(String type: TYPES) {
            HashSet<StringMap> values = new HashSet<>();
            for(String id: contacts.keySet())
                if(contacts.get(id).get(type) != null 
                    && contacts.get(id).get(type).size() > 0)
                    values.addAll(contacts.get(id).get(type));

            if(values.size() > 0)
                contact.put(type, values);
        }
        
        return contact;
    }

    public Boolean saveMergedContact (HashMap<String,HashSet<StringMap>> mergedContact) {
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
                    if((!type.equals(TYPE_GROUP) || groupInc)
                       && (!type.equals(TYPE_PHOTO) || photoInc)) {
                        HashSet<StringMap> dels;
                        HashSet<StringMap> adds;

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
                        for (StringMap item : dels) {
                            opBuilder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
                            ArrayList<String> selection = new ArrayList<>();
                            where = "? = ?";
                            selection.add(Data.RAW_CONTACT_ID);
                            selection.add(id);
                            
                            where += " AND ? = ?";
                            selection.add(Data.MIMETYPE);
                            selection.add(type);
                            
                            for(String field: CONTACT_FIELDS) {
                                if(!item.isByteArray(field) && item.get(field) != null) {
                                    where += " AND ? = ?";
                                    selection.add(field);
                                    selection.add(item.get(field));
                                }
                            }

                            opBuilder.withSelection(where, selection.toArray(new String[selection.size()]));
                            ops.add(opBuilder.build());
                        }
                        for (StringMap item : adds) {
                            opBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                    .withValue(Data.RAW_CONTACT_ID, id)
                                    .withValue(Data.MIMETYPE, type);
                            for(String field: CONTACT_FIELDS) {
                                if(item.getObject(field) != null)
                                    opBuilder.withValue(field, item.getObject(field));
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

        removeEntries();
        Boolean first = true;
        for (String id: list) {
            if (accounts.get(id).equals(account1Name)) {
                uName = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
            } else if (accounts.get(id).equals(account2Name)) {
                uName = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;
            }

            String name = contacts.get(id).get(TYPE_NAME).iterator().next().get("value");
            
            
            if (listName.startsWith(Match.DUPKEY)) {
                
                for(String type: Match.MIME_TYPE_LIST) {
                    if (findEntries(Match.DUPKEY + type + accounts.get(id), id))
                        return;
                }
            }
            addEntry(uName, name + ":" + id);
            if (first && listName.startsWith(Match.MATCHEDKEY)) {
                first = false;
                String id1, id2;
                if (accounts.get(id).equals(account1Name)) {
                    id1 = id;
                    id2 = listMap.get(id);
                } else {
                    id1 = listMap.get(id);
                    id2 = id;
                }
                if(listName.equals(Match.MATCHEDKEY + account1Name + ":" + account2Name)) {
                    removeEntry(Match.MATCHEDKEY + account1Name + ":" + account2Name, id2, id1);
                    removeEntry(Match.MATCHEDKEY + account2Name + ":" + account1Name, id1, id2);
                } else {
                    for(String type: Match.MIME_TYPE_LIST) {
                        removeEntry(Match.MATCHEDKEY + type + account1Name + ":" + account2Name, id2, id1);
                        removeEntry(Match.MATCHEDKEY + type + account2Name + ":" + account1Name, id1, id2);
                    }
                }
            }
        }
    }

    public void addToMatched () {
        String matchedName;
        String uName;
        String name;

        for (String id1: account1) {
            for (String id2: account2) {
                if(listName.startsWith(Match.UNMATCHNAMEKEY)) {
                    name = contacts.get(id1).get(TYPE_NAME).iterator().next().get("value");
                    uName = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
                    removeEntry(uName, id1, name);

                    name = contacts.get(id2).get(TYPE_NAME).iterator().next().get("value");
                    uName = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;
                    removeEntry(uName, id2, name);
                } else if (listName.startsWith(Match.MATCHEDKEY)) {
                    removeEntry(listName,id2,id1);
                    for(String type: Match.MIME_TYPE_LIST) {
                        removeEntry(Match.MATCHEDKEY + type + account1Name + ":" + account2Name, id2, id1);
                        removeEntry(Match.MATCHEDKEY + type + account2Name + ":" + account1Name, id1, id2);
                    }
                }
            

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
                    .withValue(CommonDataKinds.Relation.TITLE, value)
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

    private Boolean findEntries(String listRef, String id) {
        HashSet<String> set = (HashSet<String>) pref.getStringSet(listRef, null);

        if (set == null || set.size() == 0)
            return false;

        for (String item : set) {
            if (item.contains(":" + id + ",")
                    || item.contains("," + id + ",")
                    || item.endsWith("," + id))
                return true;
        }
        return false;
    }

    private void removeEntries() {
        HashSet<String> set = (HashSet<String>) pref.getStringSet(listName, null);

        if (set == null || set.size() == 0)
            return;

        for (String item : set) {
            if (item.startsWith(listKey)) {
                set.remove(item);
                break;
            }
        }

        SharedPreferences.Editor e = pref.edit();
        if(set.size() == 0)
            e.remove(listName);
        else
            e.putStringSet(listName, set);
        e.apply();
    }

    private void removeEntry(String listRef, String id) {
        HashSet<String> set = (HashSet<String>) pref.getStringSet(listRef, null);

        if (set == null || set.size() == 0)
            return;
        
        for (String item: set) {
            //Toast.makeText(main,item,Toast.LENGTH_SHORT).show();
            String itemArray[] = item.split(":");
            ArrayList<String> idList = (ArrayList)Arrays.asList(itemArray[1].split(","));
            if(idList.contains(id)) {
                idList.remove(id);
                set.remove(item);
                if(idList.size() > 0) {
                    item = itemArray[0];
                    for(String itemId: idList)
                        item += itemId + ",";
                    item = item.substring(0,item.length()-1);
                    set.add(item);
                }
                break;
            }
        }
        SharedPreferences.Editor e = pref.edit();
        if(set.size() == 0)
            e.remove(listRef);
        else
            e.putStringSet(listRef, set);
        e.apply();
    }

    private void removeEntry(String listRef, String ref1, String ref2) {
        HashSet<String> set = (HashSet<String>) pref.getStringSet(listRef, null);
        Toast.makeText(main,listRef + " " + ref1 + " " + ref2,Toast.LENGTH_SHORT).show();
        if (set == null)
            return;
        set.remove(ref2 + ":" + ref1);
        SharedPreferences.Editor e = pref.edit();
        if(set.size() == 0)
            e.remove(listRef);
        else
            e.putStringSet(listRef, set);
            
        e.apply();
    }

    private void addEntry(String listRef, String entry) {
        HashSet<String> set = (HashSet<String>) pref.getStringSet(listRef, null);
        if (set == null)
            return;
        set.add(entry);
        SharedPreferences.Editor e = pref.edit();
        e.putStringSet(listRef, set);
        e.apply();
    }
}
