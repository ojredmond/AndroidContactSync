package uk.me.redmonds.contactsync;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.*;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Entity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Match
{
    public static final String SYNCMATCHED = "syncMatched";
    public static final String DUPKEY = "dup:";
    public static final String UNMATCHNAMEKEY = "unmatchedName:";
    public static final String MATCHEDKEY = "matched:";
    public static final String ACCOUNTKEY = "account:";
    public static final String NUMCONTACTS = "count:";
    public static final String MIME_TYPE_LIST[] = {
            StructuredName.CONTENT_ITEM_TYPE,
            Phone.CONTENT_ITEM_TYPE,
            Email.CONTENT_ITEM_TYPE,
            Nickname.CONTENT_ITEM_TYPE,
            SipAddress.CONTENT_ITEM_TYPE
    };
    private MainActivity mainActivity = null;
    private TextView status = null;
    private String syncType = "";
    private HashMap<String, Long> account1;
    private HashMap<String, HashMap<String, Long>> account1Other;
    private HashMap<String, Long> account2;
    private HashMap<String, HashMap<String, Long>> account2Other;
    private HashMap<String, String> dup1List;
    private HashMap<String, HashMap<String, String>> dup1ListOther;
    private HashMap<String, String> dup2List;
    private HashMap<String, HashMap<String, String>> dup2ListOther;
    private HashMap<String, Long> unmatched1;
    private HashMap<String, Long> unmatched2;
    private HashMap<Long, Long>  matched1;
    private HashMap<Long, Long>  matched2;
    private String account1Name;
    private String account2Name;
    private Boolean syncMatched;

    public void startMatch(MainActivity main, View statusView, String type) {
        mainActivity = main;
        syncType = type;
        status = (TextView) statusView.findViewById(R.id.statuslog);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
        account1Name = settings.getString(MainActivity.ACCOUNT1, null);
        account2Name = settings.getString(MainActivity.ACCOUNT2, null);

        MatchContacts task = new MatchContacts();
        task.execute();
    }

    private class MatchContacts extends AsyncTask<Void, String, String> {
        private int numContactsAccount1 = -1;
        private int numContactsAccount2 = -1;
        private Boolean syncStarted = false;

        @Override
        protected String doInBackground(Void... params) {
            String message;
            String tempContactName;
            Long tempContactId;
            String tempData;

            account1 = new HashMap<>();
            account2 = new HashMap<>();
            dup1List = new HashMap<>();
            dup2List = new HashMap<>();
            unmatched1 = new HashMap<>();
            unmatched2 = new HashMap<>();
            matched1 = new HashMap<>();
            matched2 = new HashMap<>();
            account1Other = new HashMap<>();
            account2Other = new HashMap<>();
            dup1ListOther = new HashMap<>();
            dup2ListOther = new HashMap<>();

            //create empty Hashmaps for Other variables
            for(String type: MIME_TYPE_LIST) {
                account1Other.put(type,new HashMap<>());
                account2Other.put(type,new HashMap<>());
                dup1ListOther.put(type,new HashMap<>());
                dup2ListOther.put(type,new HashMap<>());
            }
            
            Cursor cursor;
            Cursor cItems;
            int matches = 0;
            int dupCount1 = 0;
            int dupCount2 = 0;
            int unmatchedCount1 = 0;
            int unmatchedCount2 = 0;

            // get sync status
            SharedPreferences status = mainActivity.getPreferences(Context.MODE_PRIVATE);
            syncMatched = status.getBoolean(SYNCMATCHED, false);

            if (syncMatched) {
                return null;
            }

            //creates mime types list for query
            String types = "";
            for(int i=0; i<MIME_TYPE_LIST.length;i++)
                types += "'" + MIME_TYPE_LIST[i] + "',";
            types = types.substring(0, types.length() - 1);
            
            message = "Starting " + syncType + "...\n";
            message += "Loading Account 1\n";
            publishProgress(message);

            ContentResolver mContentResolver = mainActivity.getContentResolver();
            Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
                    .appendQueryParameter(RawContacts.DELETED, "0")
                    .build();

            cursor = mContentResolver.query(
                    rawContactUri,
                    new String[]{RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY},
                    RawContacts.ACCOUNT_NAME + "==? AND " + RawContacts.ACCOUNT_TYPE + "==?",
                    new String[]{account1Name, MainActivity.ACCOUNT_TYPE}, RawContacts.DISPLAY_NAME_PRIMARY);

            Uri tmpUri = RawContacts.CONTENT_URI.buildUpon()
                    .appendQueryParameter(RawContacts.ACCOUNT_NAME, account1Name)
                    .appendQueryParameter(RawContacts.ACCOUNT_TYPE, MainActivity.ACCOUNT_TYPE)
                    .build();
            Uri entityUri = Uri.withAppendedPath(tmpUri, Entity.CONTENT_DIRECTORY);
            Cursor c = mContentResolver.query(entityUri,
                      new String[]{RawContacts.SOURCE_ID, Entity.DATA_ID, Entity.MIMETYPE, Entity.DATA1},
                      null, null, null);
            try {
                while (c.moveToNext()) {
                    String sourceId = c.getString(0);
                    if (!c.isNull(1)) {
                        String mimeType = c.getString(2);
                        String data = c.getString(3);
                    }
                }
            } finally {
                c.close();
            }
            /*cursor = mContentResolver.query(entityUri,
                    new String[]{RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY, Entity.MIMETYPE, Entity.DATA1},
                    RawContacts.ACCOUNT_NAME + "==? AND " 
                        + RawContacts.ACCOUNT_TYPE + "==? AND ",
                        + "==? AND " + Entity.MIMETYPE + " IN (" + types + ") AND " 
                        + RawContacts.DELETED + "==0",
                    new String[]{account1Name, MainActivity.ACCOUNT_TYPE}, null);*/

            cursor.moveToFirst();
            numContactsAccount1 = cursor.getCount();

            while (!cursor.isAfterLast()) {
                //if(cursor.getString(4) != null && cursor.getString(4).equals(StructuredName.CONTENT_ITEM_TYPE)
                //    && cursor.getString(4).equals(account1Name) && cursor.getString(5).equals(MainActivity.ACCOUNT_TYPE)) {
                tempContactName = cursor.getString(1);
                tempContactId = cursor.getLong(0);

                /*cItems = mContentResolver.query(Data.CONTENT_URI,
                    new String[]{Data.RAW_CONTACT_ID, Data.MIMETYPE, Data.DATA1},
                    Data.RAW_CONTACT_ID + "==? AND " + Data.MIMETYPE + " IN (" + types + ")",
                    new String[]{String.valueOf(tempContactId)}, null);
    
                cItems.moveToFirst();
                while (!cItems.isAfterLast()) {
                    account1Other.get(cItems.getString(1)).put(cItems.getString(2),cursor.getLong(0));
                    cItems.moveToNext();
                }*/

                if (dup1List.containsKey(tempContactName)) {
                    dupCount1++;
                    dup1List.put(tempContactName, dup1List.get(tempContactName) + "," + Long.toString(tempContactId));
                } else if (account1.containsKey(tempContactName)) {
                    dupCount1++;
                    dup1List.put(tempContactName, account1.get(tempContactName) + "," + Long.toString(tempContactId));
                    account1.remove(tempContactName);
                } else
                    account1.put(tempContactName, tempContactId);

                cursor.moveToNext();
            //}
            }

            cursor.close();

            message = "Loaded Account 1: "
                    + String.valueOf(numContactsAccount1)
                    + " contacts\n";
            message += "Account 1 Duplicates: " + String.valueOf(dupCount1) + "\n";
            message += "Loading Account 2\n";

            if (account2Name.equals(account1Name))
                return message;
            else
                publishProgress(message);

            cursor = mContentResolver.query(
                    rawContactUri,
                    new String[]{RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY},
                    RawContacts.ACCOUNT_NAME + "==? AND " + RawContacts.ACCOUNT_TYPE + "==?",
                    new String[]{account2Name, MainActivity.ACCOUNT_TYPE}, RawContacts.DISPLAY_NAME_PRIMARY);

            cursor.moveToFirst();
            numContactsAccount2 = cursor.getCount();

            while (!cursor.isAfterLast()) {
                tempContactName = cursor.getString(1);
                tempContactId = cursor.getLong(0);

                if (dup2List.containsKey(tempContactName)) {
                    dupCount2++;
                    dup1List.put(tempContactName, dup1List.get(tempContactName) + "," + Long.toString(tempContactId));
                } else if (account2.containsKey(tempContactName)) {
                    dupCount2++;
                    dup1List.put(tempContactName, account2.get(tempContactName) + "," + Long.toString(tempContactId));
                    account2.remove(tempContactName);
                } else if (account1.containsKey(tempContactName)) {
                    account2.put(tempContactName, tempContactId);
                    matches++;
                    matched1.put(account1.get(tempContactName), tempContactId);
                    matched2.put(tempContactId, account1.get(tempContactName));
                } else {
                    account2.put(tempContactName, tempContactId);
                    unmatchedCount2++;
                    unmatched2.put(tempContactName, tempContactId);
                }

                cursor.moveToNext();
            }

            cursor.close();

            message = "Loaded Account 2: "
                    + String.valueOf(numContactsAccount2)
                    + " contacts\n";
            message += "Account 2 Duplicates: " + String.valueOf(dupCount2) + "\n";
            publishProgress(message);

            for (HashMap.Entry<String, Long> entry : account1.entrySet()) {
                if (!account2.containsKey(entry.getKey())) {
                    unmatchedCount1++;
                    unmatched1.put(entry.getKey(), entry.getValue());
                }
            }

            message = "Results:\n";
            message += "Matched: " + String.valueOf(matches) + "\n";
            message += "Unmatched from account 1: " + String.valueOf(unmatchedCount1) + "\n";
            message += "Unmatched from account 2: " + String.valueOf(unmatchedCount2) + "\n";
            return message;
        }

        @Override
        protected void onProgressUpdate(String... message) {
            if (syncStarted) {
                status.append(message[0]);
            } else {
                status.setText(message[0]);
                syncStarted = true;
            }
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);

            if (syncMatched) {
                return;
            }
            if (!message.equals("")) {
                status.append(message);
            }

            SharedPreferences.Editor results = mainActivity.getPreferences(Context.MODE_PRIVATE).edit();

            //store the number of contacts for account1 so that can display results even if no contacts
            results.putInt(NUMCONTACTS + account1Name, numContactsAccount1);
            results.apply();

            HashSet<String> dup1Name = new HashSet<>();
            for (Map.Entry<String, String> e : dup1List.entrySet()) {
                dup1Name.add(e.getKey() + ":" + e.getValue());
            }
            results.putStringSet(DUPKEY + account1Name, dup1Name);
            results.apply();

            HashSet<String> account1Set = new HashSet<>();
            for (Map.Entry<String, Long> e : account1.entrySet()) {
                account1Set.add(String.valueOf(e.getValue()) + ":" + e.getKey());
            }
            results.putStringSet(ACCOUNTKEY + account1Name, account1Set);
            results.apply();

            if (!account2Name.equals(account1Name)) {
                //store the number of contacts for account2 so that can display results even if no contacts
                results.putInt(NUMCONTACTS + account2Name, numContactsAccount2);
                results.apply();

                HashSet<String> dup2Name = new HashSet<>();
                for (Map.Entry<String, String> e : dup2List.entrySet()) {
                    dup2Name.add(e.getKey() + ":" + e.getValue());
                }
                results.putStringSet(DUPKEY + account2Name, dup2Name);
                results.apply();

                HashSet<String> unmatched1Name = new HashSet<>();
                for (Map.Entry<String, Long> e : unmatched1.entrySet()) {
                    unmatched1Name.add(e.getKey() + ":" + e.getValue());
                }
                results.putStringSet(UNMATCHNAMEKEY + account1Name + ":" + account2Name, unmatched1Name);

                results.apply();

                HashSet<String> unmatched2Name = new HashSet<>();
                for (Map.Entry<String, Long> e : unmatched2.entrySet()) {
                    unmatched2Name.add(e.getKey() + ":" + e.getValue());
                }
                results.putStringSet(UNMATCHNAMEKEY + account2Name + ":" + account1Name, unmatched2Name);

                results.apply();

                HashSet<String> matched1Name = new HashSet<>();
                for (Map.Entry<Long, Long> e : matched1.entrySet()) {
                    matched1Name.add(e.getKey() + ":" + e.getValue());
                }
                results.putStringSet(MATCHEDKEY + account1Name + ":" + account2Name, matched1Name);
                results.apply();

                HashSet<String> matched2Name = new HashSet<>();
                for (Map.Entry<Long, Long> e : matched2.entrySet()) {
                    matched2Name.add(e.getKey() + ":" + e.getValue());
                }
                results.putStringSet(MATCHEDKEY + account2Name + ":" + account1Name, matched2Name);
                results.apply();

                HashSet<String> account2Set = new HashSet<>();
                for (Map.Entry<String, Long> e : account2.entrySet()) {
                    account2Set.add(String.valueOf(e.getValue()) + ":" + e.getKey());
                }
                results.putStringSet(ACCOUNTKEY + account2Name, account2Set);
                results.apply();
            }

            results.putBoolean(SYNCMATCHED, true);
            results.apply();

            syncMatched = true;
            for(String type: MIME_TYPE_LIST) {
                Toast.makeText(mainActivity, type + account1Other.get(type).size(),Toast.LENGTH_SHORT).show();
            }

            mainActivity.showResults();
        }
    }
}
