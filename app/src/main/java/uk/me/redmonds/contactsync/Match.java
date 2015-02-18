package uk.me.redmonds.contactsync;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.RawContacts;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Match
{
    private MainActivity mainActivity = null;
    private TextView status = null;
    private String syncType = "";
    private HashMap<String, Long> account1;
    private HashMap<String, Long> account2;
    private HashMap<String, String> dup1List;
    private HashMap<String, String> dup2List;
    private HashMap<String, Long> unmatched1;
    private HashMap<String, Long> unmatched2;
    private HashMap<Long, Long>  matched1;
    private HashMap<Long, Long>  matched2;
    private String account1Name;
    private String account2Name;
    private Boolean syncMatched;
    public static final String SYNCMATCHED = "syncMatched";
    public static final String DUPKEY = "dup:";
    public static final String UNMATCHNAMEKEY = "unmatchedName:";
    public static final String MATCHEDKEY = "matched:";
    public static final String ACCOUNTKEY = "account:";
    public static final String NUMCONTACTS = "count:";

    private class MatchContacts extends AsyncTask <Void, String, String> {
        private int numContactsAccount1 = -1;
        private int numContactsAccount2 = -1;
        private String lastContactName = "";
        private String tempContactName = "";
        private Long tempContactId;
        private Boolean dup = false;
        private Boolean syncStarted = false;

        @Override
        protected String doInBackground (Void... params) {
            String message;

            account1 = new HashMap<String, Long> ();
            account2 = new HashMap<String, Long> ();
            dup1List = new HashMap<String, String> ();
            dup2List = new HashMap<String, String> ();
            unmatched1 = new HashMap<String, Long> ();
            unmatched2 = new HashMap<String, Long> ();
            matched1 = new HashMap<Long, Long> ();
            matched2 = new HashMap<Long, Long> ();

            Cursor cursor;
            int matches = 0;
            int dupCount1 = 0;
            int dupCount2 = 0;
            int unmatchedCount1 = 0;
            int unmatchedCount2 = 0;
            Long contactId;

            // get sync status
            SharedPreferences status = mainActivity.getPreferences(Context.MODE_PRIVATE);
            syncMatched = status.getBoolean(SYNCMATCHED, false);

            if (syncMatched) {
                return null;
            }
            message = "Starting " + syncType + "...\n";
            message += "Loading Account 1\n";
            publishProgress(new String[] {message});

            ContentResolver mContentResolver = mainActivity.getContentResolver();
            Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(RawContacts.ACCOUNT_NAME, account1Name)
                .appendQueryParameter(RawContacts.ACCOUNT_TYPE, MainActivity.TYPE)
                .appendQueryParameter(RawContacts.DELETED, 0)
                .build();
            
            Cursor cursor = getContentResolver().query(
                rawContactUri,
                new String[]{RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY,
                null, null, RawContacts.DISPLAY_NAME_PRIMARY);
            
            //new String[]{RawContacts.SOURCE_ID, Entity.DATA_ID, Entity.MIMETYPE, Entity.DATA1}
            /*cursor = mContentResolver.query(
                    RawContacts.CONTENT_URI,
                    new String[]{RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY},
                    RawContacts.ACCOUNT_TYPE + " == '" + MainActivity.TYPE + "'"
                            + " AND " + RawContacts.ACCOUNT_NAME + " == '" + account1Name + "' "
                            + " AND " + RawContacts.DELETED + " == 0",
                    null, RawContacts.DISPLAY_NAME_PRIMARY);*/

            cursor.moveToFirst();
            numContactsAccount1 = cursor.getCount();

            while (!cursor.isAfterLast()) {
                tempContactName = cursor.getString(1);
                tempContactId = cursor.getLong(0);

                if (lastContactName.equals(tempContactName)) {
                    dup = true;
                }

                cursor.moveToNext();

                if (!cursor.isAfterLast()) {
                    if (tempContactName.equals(cursor.getString(1))) {
                        dup = true;
                    }
                }

                if (dup) {
                    dupCount1++;
                    if (dup1List.containsKey(tempContactName))
                        dup1List.put(tempContactName, dup1List.get(tempContactName) + "," + Long.toString(tempContactId));
                    else
                        dup1List.put(tempContactName, Long.toString(tempContactId));
                } else {
                    account1.put(tempContactName, tempContactId);
                }
                lastContactName = tempContactName;
                dup = false;
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
                publishProgress(new String[] {message});

            cursor = mContentResolver.query(
                    RawContacts.CONTENT_URI,
                    new String[]{RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY},
                    RawContacts.ACCOUNT_TYPE + " == '" + MainActivity.TYPE + "'"
                            + " AND " + RawContacts.ACCOUNT_NAME + " == '" + account2Name + "' "
                            + " AND " + RawContacts.DELETED + " == 0",
                    null, RawContacts.DISPLAY_NAME_PRIMARY);

            cursor.moveToFirst();
            numContactsAccount2 = cursor.getCount();

            while (!cursor.isAfterLast()) {
                tempContactName = cursor.getString(1);
                tempContactId = cursor.getLong(0);

                if (lastContactName.equals(tempContactName)) {
                    dup = true;
                }

                cursor.moveToNext();

                if (!cursor.isAfterLast()) {
                    if (tempContactName.equals(cursor.getString(1))) {
                        dup = true;
                    }
                }

                if (dup) {
                    dupCount2++;
                    if (dup2List.containsKey(tempContactName))
                        dup2List.put(tempContactName, dup2List.get(tempContactName) + "," + Long.toString(tempContactId));
                    else
                        dup2List.put(tempContactName, Long.toString(tempContactId));
                } else {
                    account2.put(tempContactName, tempContactId);

                    //match records between account 1 and 2
                    if (account1.containsKey(tempContactName)) {
                        contactId = account1.get(tempContactName);
                        matches++;
                        matched1.put(contactId, tempContactId);
                        matched2.put(tempContactId, contactId);
                    } else {
                        unmatchedCount2++;
                        unmatched2.put(tempContactName, tempContactId );
                    }
                }
                lastContactName = tempContactName;
                dup = false;
            }

            cursor.close();

            message = "Loaded Account 2: "
                    + String.valueOf(numContactsAccount2)
                    + " contacts\n";
            message += "Account 2 Duplicates: " + String.valueOf(dupCount2) + "\n";
            publishProgress(new String[] {message});

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
            if (message != "") {
                status.append(message);
            }

            SharedPreferences.Editor results = mainActivity.getPreferences(Context.MODE_PRIVATE).edit();

            //store the number of contacts i each account so that can display results even if no contacts
            results.putInt(NUMCONTACTS + account1Name, numContactsAccount1);
            results.putInt(NUMCONTACTS + account2Name, numContactsAccount2);
            results.commit();

            HashSet<String> dup1Name = new HashSet<String>();
            for (Map.Entry <String, String> e : dup1List.entrySet()) {
                dup1Name.add(e.getKey() + ":" + e.getValue());
            }
            results.putStringSet(DUPKEY + account1Name, dup1Name);
            results.commit();

            HashSet<String> dup2Name = new HashSet<String>();
            for (Map.Entry <String, String> e : dup2List.entrySet()) {
                dup2Name.add(e.getKey() + ":" + e.getValue());
            }
            results.putStringSet(DUPKEY + account2Name, dup2Name);
            results.commit();

            HashSet<String> unmatched1Name = new HashSet<String>();
            for (Map.Entry <String, Long> e : unmatched1.entrySet()) {
                unmatched1Name.add(e.getKey() + ":" + e.getValue());
            }
            results.putStringSet(UNMATCHNAMEKEY + account1Name + ":" + account2Name, unmatched1Name);

            results.commit();

            HashSet<String> unmatched2Name = new HashSet<String>();
            for (Map.Entry <String, Long> e : unmatched2.entrySet()) {
                unmatched2Name.add(e.getKey() + ":" + e.getValue());
            }
            results.putStringSet(UNMATCHNAMEKEY + account2Name + ":" + account1Name, unmatched2Name);

            results.commit();

            HashSet<String> matched1Name = new HashSet<String>();
            for (Map.Entry <Long, Long> e : matched1.entrySet()) {
                matched1Name.add(e.getKey() + ":" + e.getValue());
            }
            results.putStringSet(MATCHEDKEY + account1Name + ":" + account2Name, matched1Name);
            results.commit();

            HashSet<String> matched2Name = new HashSet<String>();
            for (Map.Entry <Long, Long> e : matched2.entrySet()) {
                matched2Name.add(e.getKey() + ":" + e.getValue());
            }
            results.putStringSet(MATCHEDKEY + account2Name + ":" + account1Name, matched2Name);
            results.commit();

            HashSet<String> account1Set = new HashSet<String>();
            for (Map.Entry <String, Long> e : account1.entrySet()) {
                account1Set.add(String.valueOf(e.getValue()) + ":" + e.getKey());
            }
            results.putStringSet(ACCOUNTKEY + account1Name, account1Set);
            results.commit();

            HashSet<String> account2Set = new HashSet<String>();
            for (Map.Entry <String, Long> e : account2.entrySet()) {
                account2Set.add(String.valueOf(e.getValue()) + ":" + e.getKey());
            }
            results.putStringSet(ACCOUNTKEY + account2Name, account2Set);
            results.commit();

            results.putBoolean(SYNCMATCHED, true);
            results.commit();

            syncMatched = true;

            mainActivity.showResults();
        }
    }

    public void startMatch(MainActivity main, View statusView, String type) {
        mainActivity = main;
        syncType = type;
        status = (TextView)statusView.findViewById(R.id.statuslog);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
        account1Name = settings.getString(MainActivity.ACCOUNT1, null);
        account2Name = settings.getString(MainActivity.ACCOUNT2, null);

        MatchContacts task = new MatchContacts();
        task.execute();
    }
}
