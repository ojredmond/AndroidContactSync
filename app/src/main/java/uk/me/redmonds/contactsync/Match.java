package uk.me.redmonds.contactsync;

//import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract.RawContacts;
import android.view.View;
import android.widget.TextView;
import android.util.SparseIntArray;
import android.util.SparseArray;
import android.preference.*;
import android.content.*;

import java.util.HashMap;
import java.util.*;

public class Match
{
    private MainActivity mainActivity = null;
    private TextView status = null;
    private String syncType = "";
    private HashMap<String, Integer> account1;
    private HashMap<String, Integer> account2;
    private SparseArray<String> dup1;
    private SparseArray<String> dup2;
    private SparseArray<String> unmatched1;
    private SparseArray<String> unmatched2;
    private SparseIntArray matched1;
    private SparseIntArray matched2;
    private String account1Name;
    private String account2Name;
    private Boolean syncMatched;
    public static final String SYNCMATCHED = "syncMatched";
    public static final String DUPKEY = "dup:";
    public static final String UNMATCHNAMEKEY = "unmatchedName:";
    public static final String MATCHEDKEY = "matched:";
    public static final String ACCOUNTKEY = "account:";

    private class MatchContacts extends AsyncTask <Void, String, String> {
        private int numContacts = -1;
        private String lastContactName = "";
        private String tempContactName = "";
        private int tempContactId = 0;
        private Boolean dup = false;
        private Boolean syncStarted = false;

        @Override
        protected String doInBackground (Void... params) {
            String message;

            account1 = new HashMap<String, Integer> ();
            account2 = new HashMap<String, Integer> ();
            dup1 = new SparseArray<String>();
            dup2 = new SparseArray<String>();
            unmatched1 = new SparseArray<String>();
            unmatched2 = new SparseArray<String>();
            matched1 = new SparseIntArray();
            matched2 = new SparseIntArray();

            Cursor cursor;
            int matches = 0;
            int dupCount1 = 0;
            int dupCount2 = 0;
            int unmatchedCount1 = 0;
            int unmatchedCount2 = 0;
            int contactId;

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
            cursor = mContentResolver.query(
                    RawContacts.CONTENT_URI,
                    new String[]{RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY},
                    RawContacts.ACCOUNT_TYPE + " == '" + getString(R.string.type) + "'"
                            + " AND " + RawContacts.ACCOUNT_NAME + " == '" + account1Name + "' "
                            + " AND " + RawContacts.DELETED + " == 0",
                    null, RawContacts.DISPLAY_NAME_PRIMARY);

            cursor.moveToFirst();
            numContacts = cursor.getCount();

            while (!cursor.isAfterLast()) {
                tempContactName = cursor.getString(1);
                tempContactId = cursor.getInt(0);

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
                    dup1.put(tempContactId, tempContactName);
                } else {
                    account1.put(tempContactName, tempContactId);
                }
                lastContactName = tempContactName;
                dup = false;
            }

            cursor.close();

            message = "Loaded Account 1: "
                    + String.valueOf(numContacts)
                    + " contacts\n";
            message += "Account 1 Duplicates: " + String.valueOf(dupCount1) + "\n";
            message += "Loading Account 2\n";
            publishProgress(new String[] {message});

            cursor = mContentResolver.query(
                    RawContacts.CONTENT_URI,
                    new String[]{RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY},
                    RawContacts.ACCOUNT_TYPE + " == '" + MainActivity.TYPE + "'"
                            + " AND " + RawContacts.ACCOUNT_NAME + " == '" + account2Name + "' "
                            + " AND " + RawContacts.DELETED + " == 0",
                    null, RawContacts.DISPLAY_NAME_PRIMARY);

            cursor.moveToFirst();
            numContacts = cursor.getCount();

            while (!cursor.isAfterLast()) {
                tempContactName = cursor.getString(1);
                tempContactId = cursor.getInt(0);

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
                    dup2.put(tempContactId, tempContactName);
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
                        unmatched2.put(tempContactId, tempContactName);
                    }
                }
                lastContactName = tempContactName;
                dup = false;
            }

            cursor.close();

            message = "Loaded Account 2: "
                    + String.valueOf(numContacts)
                    + " contacts\n";
            message += "Account 2 Duplicates: " + String.valueOf(dupCount2) + "\n";
            publishProgress(new String[] {message});

            for (HashMap.Entry<String, Integer> entry : account1.entrySet()) {
                if (!account2.containsKey(entry.getKey())) {
                    unmatchedCount1++;
                    unmatched1.put(entry.getValue(), entry.getKey());
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

            HashSet<String> dup1Name = new HashSet<String>();
            for (int i=0; i < dup1.size(); i++) {
                dup1Name.add(String.valueOf(dup1.keyAt(i)) + ":"
                        + dup1.valueAt(i));
            }
            results.putStringSet(DUPKEY + account1Name, dup1Name);
            results.commit();

            HashSet<String> dup2Name = new HashSet<String>();
            for (int i=0; i < dup2.size(); i++) {
                dup2Name.add(String.valueOf(dup2.keyAt(i)) + ":"
                        + dup2.valueAt(i));
            }
            results.putStringSet(DUPKEY + account2Name, dup2Name);
            results.commit();

            HashSet<String> unmatched1Name = new HashSet<String>();

            for (int i=0; i < unmatched1.size(); i++) {
                unmatched1Name.add(String.valueOf(unmatched1.keyAt(i)) + ":"
                        + unmatched1.valueAt(i));
            }
            results.putStringSet(UNMATCHNAMEKEY + account1Name + ":" + account2Name, unmatched1Name);

            results.commit();

            HashSet<String> unmatched2Name = new HashSet<String>();

            for (int i=0; i < unmatched2.size(); i++) {
                unmatched2Name.add(String.valueOf(unmatched2.keyAt(i)) + ":"
                        + unmatched2.valueAt(i));
            }
            results.putStringSet(UNMATCHNAMEKEY + account2Name + ":" + account1Name, unmatched2Name);

            results.commit();

            HashSet<String> matched1Name = new HashSet<String>();
            for (int i=0; i < matched1.size(); i++) {
                matched1Name.add(String.valueOf(matched1.keyAt(i))
                        + ":" + String.valueOf(matched1.valueAt(i)));
            }
            results.putStringSet(MATCHEDKEY + account1Name + ":" + account2Name, matched1Name);
            results.commit();

            HashSet<String> matched2Name = new HashSet<String>();
            for (int i=0; i < matched2.size(); i++) {
                matched2Name.add(String.valueOf(matched2.keyAt(i))
                        + ":" + String.valueOf(matched2.valueAt(i)));
            }
            results.putStringSet(MATCHEDKEY + account2Name + ":" + account1Name, matched2Name);
            results.commit();

            HashSet<String> account1Set = new HashSet<String>();
            for (Map.Entry <String, Integer> e : account1.entrySet()) {
                account1Set.add(String.valueOf(e.getValue()) + ":" + e.getKey());
            }
            results.putStringSet(ACCOUNTKEY + account1Name, account1Set);
            results.commit();

            HashSet<String> account2Set = new HashSet<String>();
            for (Map.Entry <String, Integer> e : account2.entrySet()) {
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
        account1Name = settings.getString("account1", null);
        account2Name = settings.getString("account2", null);

        MatchContacts task = new MatchContacts();
        task.execute();
    }
}