package uk.me.redmonds.contactsync;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.LongSparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private final static HashSet<String> emptySet = new HashSet<>();
    private static Boolean groupInc;
    private static Boolean photoInc;
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
    private final HashMap<String, HashMap<String, LongSparseArray<StringMap>>> contacts = new HashMap<>();
    private final HashMap<String, HashMap<String, HashMap<String, String>>> groupIds = new HashMap<>();
    private final HashMap<String, String> groupNames = new HashMap<>();


    ContactsHelper(Activity m, String l, String key, HashSet<String> ids) {
        main = (MainActivity) m;
        listName = l;
        listKey = key;
        list = ids;
        pref = main.getSharedPreferences(Match.PREFKEY, Context.MODE_PRIVATE);
        createContacts();
    }

    ContactsHelper(Activity m, String l, String key, String[] ids) {
        main = (MainActivity) m;
        listName = l;
        listKey = key;
        list = new HashSet<>();
        Collections.addAll(list, ids);
        pref = main.getSharedPreferences(Match.PREFKEY, Context.MODE_PRIVATE);
        createContacts();
    }

    public static String getGroupName(String mime) {
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
            contacts.put(i, new HashMap<String, LongSparseArray<StringMap>>());
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

        if (groupInc) {
            c = main.getContentResolver().query(
                    ContactsContract.Groups.CONTENT_URI,
                    new String[]{ContactsContract.Groups.ACCOUNT_NAME,
                            ContactsContract.Groups.ACCOUNT_TYPE,
                            ContactsContract.Groups._ID,
                            ContactsContract.Groups.TITLE,
                            ContactsContract.Groups.NOTES,
                            ContactsContract.Groups.SYSTEM_ID},
                    ContactsContract.Groups.DELETED + "!='1'",
                    null, null);


            while (c.moveToNext()) {
                if (!c.isNull(c.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE)) && c.getString(c.getColumnIndex(ContactsContract.Groups.ACCOUNT_TYPE)).equals(MainActivity.ACCOUNT_TYPE)) {
                    String id = c.getString(c.getColumnIndex(ContactsContract.Groups._ID));
                    String title = c.getString(c.getColumnIndex(ContactsContract.Groups.TITLE));
                    String account = c.getString(c.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME));
                    String system = c.getString(c.getColumnIndex(ContactsContract.Groups.SYSTEM_ID));
                    HashMap<String, String> groupItem = new HashMap<>();
                    groupItem.put("id", c.getString(c.getColumnIndex(ContactsContract.Groups._ID)));
                    groupItem.put("notes", c.getString(c.getColumnIndex(ContactsContract.Groups.NOTES)));
                    groupItem.put("system", system);

                    HashMap<String, HashMap<String, String>> group;

                    if ((groupIds.containsKey(title) && !groupIds.get(title).containsKey(account))
                            || (groupIds.containsKey(title) && groupIds.get(title).containsKey(account) && system != null)
                            ) {
                        groupIds.get(title).put(account, groupItem);
                    }

                    if (!groupIds.containsKey(title)) {
                        group = new HashMap<>();
                        group.put(account, groupItem);
                        groupIds.put(title, group);
                        //Toast.makeText(main,title + group.toString(),Toast.LENGTH_LONG).show();
                    }

                    groupNames.put(id, title);
                }
            }
        }
//Toast.makeText(main,groupNames.toString(),Toast.LENGTH_LONG).show();

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
                String id = c.getString(c.getColumnIndex(Data.RAW_CONTACT_ID));
                HashMap<String, LongSparseArray<StringMap>> contact = contacts.get(id);
                if (!c.isNull(c.getColumnIndex(Data._ID)) && !c.isNull(c.getColumnIndex(Data.MIMETYPE))
                        && (!c.getString(c.getColumnIndex(Data.MIMETYPE)).equals(TYPE_GROUP) || groupInc)
                        && (!c.getString(c.getColumnIndex(Data.MIMETYPE)).equals(TYPE_PHOTO) || photoInc)
                        && ((c.getString(c.getColumnIndex(Data.MIMETYPE)).equals(TYPE_PHOTO) && !c.isNull(CONTACT_FIELDS.length + 2)) || !c.getString(2).equals(TYPE_PHOTO))
                        ) {
                    //only add to content if row has content
                    Boolean hasContent = false;

                    StringMap value = new StringMap();
                    if (c.getString(c.getColumnIndex(Data.MIMETYPE)).equals(TYPE_GROUP)) {
                        if (c.getString(c.getColumnIndex(Data.MIMETYPE)).equals(TYPE_GROUP) && groupNames.containsKey(c.getString(c.getColumnIndex(Data.DATA1)))) {
                            String groupName = groupNames.get(c.getString(c.getColumnIndex(Data.DATA1)));
                            HashMap<String, HashMap<String, String>> group = null;
                            if (groupIds.containsKey(groupName))
                                group = groupIds.get(groupName);

                            //Toast.makeText(main,group.toString(),Toast.LENGTH_LONG).show();
                            if (group != null) {
                                value.put("group", group);
                                hasContent = true;
                            }
                            value.put("value", groupName);
                        }
                    } else {
                        //add value if Data 1 has content
                        if (!c.isNull(c.getColumnIndex(Data.DATA1)) && !c.getString(c.getColumnIndex(Data.DATA1)).equals("")) {
                            value.put("value", c.getString(c.getColumnIndex(Data.DATA1)));
                        }
                        //add label to store common extra information
                        value.put("label", getTypeLabel(c.getString(c.getColumnIndex(Data.MIMETYPE)), c.getInt(c.getColumnIndex(Data.DATA2)), c.getString(c.getColumnIndex(Data.DATA3))));

                        //loop through the data fields & store in contact
                        for (String CONTACT_FIELD : CONTACT_FIELDS) {
                            if (!c.isNull(c.getColumnIndex(CONTACT_FIELD)) && (c.getType(c.getColumnIndex(CONTACT_FIELD)) == Cursor.FIELD_TYPE_BLOB
                                    || !c.getString(c.getColumnIndex(CONTACT_FIELD)).equals("")))
                                hasContent = true;
                            if (c.getType(c.getColumnIndex(CONTACT_FIELD)) == Cursor.FIELD_TYPE_BLOB)
                                value.put(CONTACT_FIELD, c.getBlob(c.getColumnIndex(CONTACT_FIELD)));
                            else
                                value.put(CONTACT_FIELD, c.getString(c.getColumnIndex(CONTACT_FIELD)));
                        }
                    }

                    if (hasContent) {
                        if (!contact.containsKey(c.getString(c.getColumnIndex(Data.MIMETYPE))))
                            contact.put(c.getString(c.getColumnIndex(Data.MIMETYPE)), new LongSparseArray<StringMap>());
                        LongSparseArray<StringMap> field = contact.get(c.getString(c.getColumnIndex(Data.MIMETYPE)));
                        field.put(c.getLong(c.getColumnIndex(Data._ID)), value);
                    }
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
        for (String item : set) {
            listMap.put(item.split(":")[0], item.split(":")[1]);
            listMap.put(item.split(":")[1], item.split(":")[0]);
        }
    }

    private String getTypeLabel(String mime, Integer type, CharSequence label) {
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

        return (String) label;
    }

    /*public HashMap<String,HashMap<String,HashSet<StringMap>>> getContacts() {
        return contacts;
    }*/

    String getAccountName(String id) {
        return accounts.get(id);
    }

    byte[] getPhoto(String contactId) {
        if (contacts.get(contactId).get(TYPE_PHOTO) == null)
            return null;

        return contacts.get(contactId).get(TYPE_PHOTO).valueAt(0).getByteArray(Data.DATA15);
    }

    int size() {
        return contacts.size();
    }

    public View getContactView(ViewGroup layoutContainer, String name, String id) {
        String account = getAccountName(id);
        // create a new view for the contact
        View contactView = LayoutInflater.from(main)
                .inflate(R.layout.contact, layoutContainer, false);
        contactView.setTag(account + ":" + id);
        LinearLayout contactInfo = (LinearLayout) contactView.findViewById(R.id.contact_info);

        // Display photo if it exists
        if (ContactsHelper.photoInc) {
            byte[] photoData = getPhoto(id);
            if (photoData != null) {
                View photoView = LayoutInflater.from(main)
                        .inflate(R.layout.image, layoutContainer, false);
                ImageView photo = (ImageView) photoView.findViewById(R.id.photo);
                Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
                photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight() / 2);
                photo.setImageBitmap(photoBitmap);
                contactInfo.addView(photoView);
            }
        }

        // Display account name
        FrameLayout deleteLayout = new FrameLayout(main);
        deleteLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        View accountInfo = LayoutInflater.from(main)
                .inflate(R.layout.list_account, layoutContainer, false);
        ((TextView) accountInfo.findViewById(R.id.type)).setText("Account");
        ((TextView) accountInfo.findViewById(R.id.value)).setText(getAccountName(id));

        // add action buttons
        View buttons = LayoutInflater.from(main)
                .inflate(R.layout.buttons, layoutContainer, false);
        ImageButton deleteButton = (ImageButton) buttons.findViewById(R.id.delete_button);

        // add delete listener
        deleteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View p1) {
                if (p1.getId() == R.id.delete_button) {
                    HashSet<String> idSet = new HashSet<>();
                    idSet.add((String) p1.getTag());
                    deleteContacts(idSet);

                    if (size() > 1) {
                        View contact = (View) p1.getParent().getParent().getParent();
                        ((ViewGroup) contact.getParent()).removeView(contact);
                    } else {
                        if (size() == 1)
                            addToUnmatched();

                        //reload comparefragement
                        main.Compare(null, listName, null);
                    }
                }
            }
        });
        //store Id in tag
        deleteButton.setTag(id);

        ImageButton editButton = (ImageButton) buttons.findViewById(R.id.edit_button);

        // add delete listener
        editButton.setOnClickListener(new OnClickListener() {
            public void onClick(View p1) {
                if (p1.getId() == R.id.edit_button) {
                    String tag[] = ((String) p1.getTag()).split(":");
                    String id[] = {tag[1]};

                    //use merge fragment for editing
                    main.Merge(tag[0], id, listName);
                }
            }
        });
        //store Id in tag
        editButton.setTag(name + ":" + id);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;

        deleteLayout.addView(accountInfo);
        deleteLayout.addView(buttons, params);
        contactInfo.addView(deleteLayout);

        // Display contact id
        accountInfo = LayoutInflater.from(main)
                .inflate(R.layout.list_row_2, layoutContainer, false);
        ((TextView) accountInfo.findViewById(R.id.type)).setText("ID");
        ((TextView) accountInfo.findViewById(R.id.value)).setText(id);
        contactInfo.addView(accountInfo);

        HashMap<String, LongSparseArray<StringMap>> contact = contacts.get(id);
        for (String type : ContactsHelper.TYPES) {
            if (contact.get(type) != null
                    && contact.get(type).size() > 0) {

                Boolean first = true;

                for (int i = 0; i < contact.get(type).size(); i++) {
                    StringMap item = contact.get(type).valueAt(i);
                    if (item.get("value") == null)
                        break;
                    if (first) {
                        TextView contactHeading = (TextView) LayoutInflater.from(main)
                                .inflate(R.layout.list_heading, layoutContainer, false);
                        contactHeading.setText(ContactsHelper.getGroupName(type));
                        contactInfo.addView(contactHeading);
                        first = false;
                    }
                    if (item.get("label") == null) {
                        TextView contactValue = (TextView) LayoutInflater.from(main)
                                .inflate(R.layout.list_row_1, layoutContainer, false);
                        contactValue.setText(item.get("value"));
                        contactInfo.addView(contactValue);
                    } else {
                        LinearLayout rowLayout = (LinearLayout) LayoutInflater.from(main)
                                .inflate(R.layout.list_row_2, layoutContainer, false);
                        ((TextView) rowLayout.findViewById(R.id.value)).setText(item.get("value"));
                        ((TextView) rowLayout.findViewById(R.id.type)).setText(item.get("label"));
                        contactInfo.addView(rowLayout);
                    }
                }
            }
        }
        return contactView;
    }

    public void deleteContacts() {
        deleteContacts(list);
    }

    void deleteContacts(HashSet<String> delList) {
        String where;
        String[] params;
        ArrayList<ContentProviderOperation> ops = null;

        for (String id : delList) {
            where = RawContacts._ID + " = ?";
            params = new String[]{id};

            ops = new ArrayList<>();
            ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
                    .withSelection(where, params)
                    .build());

            String name = contacts.get(id).get(TYPE_NAME).valueAt(0).get("value");

            if (listName.startsWith(Match.MATCHEDKEY)) {
                String id1, id2;
                if (accounts.get(id).equals(account1Name)) {
                    id1 = id;
                    id2 = listMap.get(id);
                } else {
                    id1 = listMap.get(id);
                    id2 = id;
                }
                if (listName.equals(Match.MATCHEDKEY + account1Name + ":" + account2Name)) {
                    removeEntry(Match.MATCHEDKEY + account1Name + ":" + account2Name, id2, id1);
                    removeEntry(Match.MATCHEDKEY + account2Name + ":" + account1Name, id1, id2);
                } else {
                    for (String type : Match.MIME_TYPE_LIST) {
                        removeEntry(Match.MATCHEDKEY + type + account1Name + ":" + account2Name, id2, id1);
                        removeEntry(Match.MATCHEDKEY + type + account2Name + ":" + account1Name, id1, id2);
                    }
                }
            } else if (listName.startsWith(Match.DUPKEY)) {
                for (String type : Match.MIME_TYPE_LIST) {
                    removeEntry(Match.DUPKEY + type + accounts.get(id), id);
                }
            }
            removeEntry(listName, id, name);

            String accountList = Match.ACCOUNTKEY + getAccountName(id);
            removeEntry(accountList, id, name);

            contacts.remove(id);
            list.remove(id);
        }

        if (ops != null) {
            try {
                main.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                // send error report
                TopExceptionHandler.sendReport(main, TopExceptionHandler.generateReport(e));
            }
        }
    }

    public HashMap<String, HashSet<StringMap>> mergeContact() {
        HashMap<String, HashSet<StringMap>> contact = new HashMap<>();
        for (String type : TYPES) {
            HashSet<StringMap> values = new HashSet<>();
            for (String id : contacts.keySet())
                if (contacts.get(id).get(type) != null
                        && contacts.get(id).get(type).size() > 0)
                    for (int i = 0; i < contacts.get(id).get(type).size(); i++)
                        values.add(contacts.get(id).get(type).valueAt(i));

            if (values.size() > 0)
                contact.put(type, values);
        }

        return contact;
    }

    private void writeDisplayPhoto(long rawContactId, byte[] photo) {
        Uri rawContactPhotoUri = Uri.withAppendedPath(
                ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
        try {
            AssetFileDescriptor fd =
                    main.getContentResolver().openAssetFileDescriptor(rawContactPhotoUri, "rw");
            OutputStream os = fd.createOutputStream();
            os.write(photo);
            os.close();
            fd.close();
        } catch (IOException e) {
            // send error report
            TopExceptionHandler.sendReport(main, TopExceptionHandler.generateReport(e));
        }
    }

    public Boolean saveMergedContact(HashMap<String, HashSet<StringMap>> mergedContact) {
        ArrayList<ContentProviderOperation> ops;
        ContentProviderOperation.Builder opBuilder;
        //String value;
        //String origValue;
        HashSet<String> accountsUsed = new HashSet<>();
        HashMap<String, HashSet<StringMap>> tmpMContact;
        if (accounts.size() == 0) {
            return false;
        }

        Boolean addToUnmatched = true;
        for (String id : list) {

            if (list.size() == 1
                    && listName.startsWith(Match.DUPKEY)) {
                String tmp[] = getListEntries();
                String tmpType = listName.substring(Match.DUPKEY.length(),
                        listName.length() - accounts.get(id).length());
                for (StringMap tmpMap : mergedContact.get(tmpType))
                    if (tmpMap.get("value").equals(listKey))
                        addToUnmatched = false;

                if (addToUnmatched && tmp.length == 2) {
                    HashSet<String> tmpList = new HashSet<>(Arrays.asList(tmp));
                    tmpList.remove(id);
                    ContactsHelper tmpContactHelper = new ContactsHelper(main, listName, listKey, tmpList);
                    tmpContactHelper.addToUnmatched();
                }
            }

            //clone merged contact
            tmpMContact = new HashMap<>();
            for (Map.Entry<String, HashSet<StringMap>> item : mergedContact.entrySet())
                tmpMContact.put(item.getKey(), item.getValue()).clone();
            if (accountsUsed.contains(accounts.get(id))) {
                HashSet<String> deList = new HashSet<>();
                deList.add(id);
                deleteContacts(deList);
            } else {
                accountsUsed.add(accounts.get(id));
                ops = new ArrayList<>();
                for (String type : TYPES) {
                    //exclude types based on settings
                    if ((!type.equals(TYPE_GROUP) || groupInc)
                            && (!type.equals(TYPE_PHOTO) || photoInc)) {
                        if (contacts.get(id).get(type) != null) {
                            for (int i = 0; i < contacts.get(id).get(type).size(); i++) {
                                Long key = contacts.get(id).get(type).keyAt(i);
                                StringMap value = contacts.get(id).get(type).valueAt(i);
                                //Toast.makeText(main,tmpMContact.get(type).size()+((StringMap)item.getValue()).get("value"),Toast.LENGTH_LONG).show();
                                if (tmpMContact.get(type).contains(value)) {
                                    //Toast.makeText(main,"found",Toast.LENGTH_LONG).show();
                                    tmpMContact.get(type).remove(value);
                                } else if (!type.equals(TYPE_GROUP) || value.get("value") == null || !value.get("value").equals("My Contacts")) {
                                    opBuilder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
                                    opBuilder.withSelection(Data._ID + "=?", new String[]{key.toString()});
                                    ops.add(opBuilder.build());
                                }
                            }
                        }

                        if (tmpMContact.get(type) != null) {
                            for (StringMap item : tmpMContact.get(type)) {
                                switch (type) {
                                    case TYPE_PHOTO:
                                        writeDisplayPhoto(Long.decode(id), item.getByteArray(PHOTO));
                                        break;
                                    case TYPE_GROUP:
                                        HashMap<String, String> group = ((HashMap<String, HashMap<String, String>>) item.getObject("group")).get(accounts.get(id));
                                        if (group == null) {
                                            int groupInsertIndex = ops.size();
                                            opBuilder = ContentProviderOperation.newInsert(ContactsContract.Groups.CONTENT_URI)
                                                    .withValue(ContactsContract.Groups.TITLE, item.get("value"))
                                                    .withValue(ContactsContract.Groups.NOTES, item.get("notes"))
                                                    .withValue(ContactsContract.Groups.GROUP_VISIBLE, 1)
                                                    .withValue(ContactsContract.Groups.ACCOUNT_TYPE, MainActivity.ACCOUNT_TYPE)
                                                    .withValue(ContactsContract.Groups.ACCOUNT_NAME, accounts.get(id));
                                            ops.add(opBuilder.build());
                                            opBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                                    .withValueBackReference(Data.DATA1, groupInsertIndex)
                                                    .withValue(Data.RAW_CONTACT_ID, id)
                                                    .withValue(Data.MIMETYPE, type);
                                            ops.add(opBuilder.build());
                                        } else {
                                            opBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                                    .withValue(Data.RAW_CONTACT_ID, id)
                                                    .withValue(Data.MIMETYPE, type)
                                                    .withValue(Data.DATA1, group.get("id"));
                                            ops.add(opBuilder.build());
                                            //Toast.makeText(main,group.get("id"),Toast.LENGTH_LONG).show();
                                        }
                                        //Toast.makeText(main,group.toString(),Toast.LENGTH_LONG).show();
                                        break;
                                    default:
                                        opBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                                                .withValue(Data.RAW_CONTACT_ID, id)
                                                .withValue(Data.MIMETYPE, type);
                                        for (String field : CONTACT_FIELDS) {
                                            if (item.getObject(field) != null)
                                                opBuilder.withValue(field, item.getObject(field));
                                        }

                                        ops.add(opBuilder.build());
                                        break;
                                }
                            }
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

        if (addToUnmatched && listName.startsWith(Match.DUPKEY))
            addToUnmatched();
        else if (!listName.startsWith(Match.DUPKEY))
            addToMatched();

        return true;
    }

    public void addToUnmatched() {
        String uName = null;

        removeEntries();
        Boolean first = true;
        for (String id : list) {
            if (accounts.get(id).equals(account1Name)) {
                uName = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
            } else if (accounts.get(id).equals(account2Name)) {
                uName = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;
            }

            String name = contacts.get(id).get(TYPE_NAME).valueAt(0).get("value");

            if (listName.startsWith(Match.DUPKEY)) {
                for (String type : Match.MIME_TYPE_LIST) {
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
                if (listName.equals(Match.MATCHEDKEY + account1Name + ":" + account2Name)) {
                    removeEntry(Match.MATCHEDKEY + account1Name + ":" + account2Name, id2, id1);
                    removeEntry(Match.MATCHEDKEY + account2Name + ":" + account1Name, id1, id2);
                } else {
                    for (String type : Match.MIME_TYPE_LIST) {
                        removeEntry(Match.MATCHEDKEY + type + account1Name + ":" + account2Name, id2, id1);
                        removeEntry(Match.MATCHEDKEY + type + account2Name + ":" + account1Name, id1, id2);
                    }
                }
            }
        }
    }

    void addToMatched() {
        String matchedName;
        String uName;
        String name;

        for (String id1 : account1) {
            for (String id2 : account2) {
                if (listName.startsWith(Match.UNMATCHNAMEKEY)) {
                    name = contacts.get(id1).get(TYPE_NAME).valueAt(0).get("value");
                    uName = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
                    removeEntry(uName, id1, name);

                    name = contacts.get(id2).get(TYPE_NAME).valueAt(0).get("value");
                    uName = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;
                    removeEntry(uName, id2, name);
                } else if (listName.startsWith(Match.MATCHEDKEY)) {
                    removeEntry(listName, id2, id1);
                    for (String type : Match.MIME_TYPE_LIST) {
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

    private String[] getListEntries() {
        HashSet<String> set = (HashSet<String>) pref.getStringSet(listName, null);

        if (set == null || set.size() == 0)
            return null;

        for (String item : set) {
            if (item.startsWith(listKey)) {
                return item.split(":")[1].split(",");
            }
        }
        return null;
    }

    private void removeEntries() {
        HashSet<String> set = new HashSet<>(pref.getStringSet(listName, emptySet));

        if (set.size() == 0)
            return;

        for (String item : set) {
            if (item.startsWith(listKey)) {
                HashSet<String> tmpIds = new HashSet<>(
                        Arrays.asList(item.split(":")[1].split(",")));

                for (String id : list)
                    tmpIds.remove(id);


                set.remove(item);
                if (tmpIds.size() > 0) {
                    item = listKey + ":";
                    for (String id : tmpIds)
                        item += id + ",";

                    item = item.substring(0, item.length() - 1);
                    set.add(item);
                }
                break;
            }
        }

        SharedPreferences.Editor e = pref.edit();
        if (set.size() == 0)
            e.remove(listName);
        else
            e.putStringSet(listName, set);
        e.apply();
    }

    private void removeEntry(String listRef, String id) {
        HashSet<String> set = new HashSet<>(pref.getStringSet(listRef, emptySet));

        if (set.size() == 0)
            return;

        for (String item : set) {
            String itemArray[] = item.split(":");
            ArrayList<String> idList = new ArrayList<>(Arrays.asList(itemArray[1].split(",")));
            if (idList.contains(id)) {
                idList.remove(id);
                set.remove(item);
                if (idList.size() > 0) {
                    item = itemArray[0] + ":";
                    for (String itemId : idList)
                        item += itemId + ",";
                    item = item.substring(0, item.length() - 1);
                    set.add(item);
                }
                break;
            }
        }
        SharedPreferences.Editor e = pref.edit();
        if (set.size() == 0)
            e.remove(listRef);
        else
            e.putStringSet(listRef, set);
        e.apply();
    }

    private void removeEntry(String listRef, String ref1, String ref2) {
        HashSet<String> set = new HashSet<>(pref.getStringSet(listRef, emptySet));
        if (set.size() == 0)
            return;
        set.remove(ref2 + ":" + ref1);
        SharedPreferences.Editor e = pref.edit();
        if (set.size() == 0)
            e.remove(listRef);
        else
            e.putStringSet(listRef, set);

        e.apply();
    }

    private void addEntry(String listRef, String entry) {
        HashSet<String> set = new HashSet<>(pref.getStringSet(listRef, emptySet));

        set.add(entry);
        SharedPreferences.Editor e = pref.edit();
        e.putStringSet(listRef, set);
        e.apply();
    }
}
