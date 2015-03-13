package uk.me.redmonds.contactsync;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import android.util.*;
import android.widget.*;

class Match {
	private static final String SYNCING = "syncing";
	public static final String LOG_TAG = "MATCH_STATUS";
    public static final String SYNCMATCHED = "syncMatched";
    public static final String DUPKEY = "dup:";
    public static final String UNMATCHNAMEKEY = "unmatchedName:";
    public static final String MATCHEDKEY = "matched:";
    public static final String ACCOUNTKEY = "account:";
    public static final String NUMCONTACTS = "count:";
    public static final String PREF_KEY_ACCOUNT = MainActivity.PACKAGE_NAME + "-duplicate";
    public static final String PREF_KEY_MATCH = MainActivity.PACKAGE_NAME + "-match";
    public static final String MIME_TYPE_LIST[] = {
            StructuredName.CONTENT_ITEM_TYPE,
            Phone.CONTENT_ITEM_TYPE,
            Email.CONTENT_ITEM_TYPE,
            Nickname.CONTENT_ITEM_TYPE,
            SipAddress.CONTENT_ITEM_TYPE
    };
    private MainActivity mainActivity = null;
    private StatusFragment status = null;
    private String syncType = "";
    private String account1Name;
    private String account2Name;
    private String accountsKey;
    private Boolean syncMatched;
    private Boolean deep;

    public void startMatch(MainActivity main, StatusFragment frag, String type) {
        mainActivity = main;
        syncType = type;
        status = frag;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
        account1Name = settings.getString(MainActivity.ACCOUNT1, null);
        account2Name = settings.getString(MainActivity.ACCOUNT2, null);
        if(account1Name.compareTo(account2Name) > 0)
            accountsKey = account1Name + account2Name;
        else
            accountsKey = account2Name + account1Name;
        
        deep = settings.getBoolean(MainActivity.DEEP, false);
		
        MatchContacts task = new MatchContacts();
        task.execute();
    }

    private class MatchContacts extends AsyncTask<Void, String, String> {
        private int numContactsAccount1 = -1;
        private int numContactsAccount2 = -1;
        private Boolean syncStarted = false;

        private Boolean performMatchingP1(
                HashMap<String, HashMap<String, String>> accountOther,
                HashMap<String, HashMap<String, String>> dupListOther,
                HashMap<String, Long> unmatched,
                HashMap<Long, String> unmatchedId,
                HashMap<String, String> tempData,
                Long tempContactId,
                String type,
                String data,
                Boolean duplicate,
                Boolean secondPass,
                HashMap<String, HashMap<Long, Long>> matched1Other,
                HashMap<String, HashMap<Long, Long>> matched2Other
        ) {
            if (dupListOther.containsKey(data)
                    && dupListOther.get(data).get(type) != null) {
                tempData.put(type, dupListOther.get(data).get(type)
                        + "," + tempContactId.toString());
                dupListOther.put(data, tempData);
                duplicate = true;
            } else if (accountOther.containsKey(data) &&
                    accountOther.get(data).get(type) != null
                    && !accountOther.get(data).get(type).equals(tempContactId.toString())) {
                //dupCount1++;
                tempData.put(type, accountOther.get(data).get(type)
                        + "," + tempContactId.toString());
                dupListOther.put(data, tempData);
                //remove from unmatched
                Long removeId = Long.decode(accountOther.get(data).get(type));
                String removeName = unmatchedId.remove(removeId);
                unmatched.remove(removeName);
                //unmatchedCount1--;
                //remove from matched
                if (secondPass && matched2Other.containsKey(type) && matched2Other.get(type).containsKey(tempContactId)) {
                    Long OtherId = matched2Other.get(type).get(tempContactId);
                    matched2Other.get(type).remove(tempContactId);
                    matched1Other.get(type).remove(OtherId);
                }
                duplicate = true;

            }

            //add all non-duplicate data to account variable
            accountOther.put(data, tempData);
            return duplicate;
        }

        private Boolean[] performMatchingP2(
                HashMap<String, HashMap<String, String>> account1Other,
                HashMap<String, HashMap<String, String>> account2Other,
                HashMap<String, HashMap<String, String>> dup2ListOther,
                HashMap<Long, String> unmatched1Id,
                HashMap<Long, String> unmatched2Id,
                HashMap<String, Long> unmatched2,
                HashMap<String, HashMap<Long, Long>> matched1Other,
                HashMap<String, HashMap<Long, Long>> matched2Other,
                HashMap<String, String> tempData,
                Long tempContactId,
                String type,
                String data,
                Boolean duplicate,
                Boolean matched
        ) {
            duplicate = performMatchingP1(
                    account2Other, dup2ListOther,
                    unmatched2, unmatched2Id,
                    tempData,
                    tempContactId, type, data,
                    duplicate, true,
                    matched1Other, matched2Other);
            if (!duplicate) {
                if (account1Other.containsKey(data)
                        && account1Other.get(data).get(type) != null
                        && !account1Other.get(data).get(type).contains(",")) {
                    Long account1id = Long.decode(account1Other.get(data).get(type));
                    if (unmatched1Id.containsKey(account1id)) {
                        HashMap<Long, Long> idsMap;
                        if (matched1Other.containsKey(type))
                            idsMap = matched1Other.get(type);
                        else
                            idsMap = new HashMap<>();

                        idsMap.put(account1id, tempContactId);
                        matched1Other.put(type, idsMap);
                        if (matched2Other.containsKey(type))
                            idsMap = matched2Other.get(type);
                        else
                            idsMap = new HashMap<>();
                        idsMap.put(tempContactId, account1id);
                        matched2Other.put(type, idsMap);

                        //matches++;
                        matched = true;
                    }
                }
            }

            //add all data to account info
            account2Other.put(data, tempData);
            return new Boolean[]{duplicate, matched};
        }

        @Override
        protected String doInBackground(Void... params) {
            String message;
            String tempContactName;
            Long tempContactId;
            HashMap<String, String> tempData;

            HashMap<Long, String> account1 = new HashMap<>();
            HashMap<Long, String> account2 = new HashMap<>();
            HashMap<String, Long> unmatched1 = new HashMap<>();
            HashMap<Long, String> unmatched1Id = new HashMap<>();
            HashMap<String, Long> unmatched2 = new HashMap<>();
            HashMap<Long, String> unmatched2Id = new HashMap<>();
            HashMap<String, HashMap<String, String>> account1Other = new HashMap<>();
            HashMap<String, HashMap<String, String>> account2Other = new HashMap<>();
            HashMap<String, HashMap<String, String>> dup1ListOther = new HashMap<>();
            HashMap<String, HashMap<String, String>> dup2ListOther = new HashMap<>();
            HashMap<String, HashMap<Long, Long>> matched1Other = new HashMap<>();
            HashMap<String, HashMap<Long, Long>> matched2Other = new HashMap<>();

            Cursor cursor;
            Cursor cItems;
            String type, data;
            Boolean duplicate, matched;

            //int matches = 0;
            int dupCount1 = 0;
            int dupCount2 = 0;
            //int unmatchedCount1 = 0;
            //int unmatchedCount2 = 0;

            // get sync status
            SharedPreferences status = mainActivity.getSharedPreferences(PREF_KEY_MATCH+accountsKey, Context.MODE_PRIVATE);
            Boolean syncStatus = status.getBoolean(SYNCING, false);
			syncMatched = status.getBoolean(SYNCMATCHED, false);

            if (syncStatus || syncMatched) {
                return null;
            }

			//store current syncing
			status.edit().putBoolean(SYNCING, true).apply();
			
			//remove duplicates and account1 contact names
			SharedPreferences.Editor results = mainActivity.getSharedPreferences(Match.PREF_KEY_ACCOUNT + account1Name, Context.MODE_PRIVATE).edit();
			results.clear();
			results.apply();

			//remove duplicates and account2 contact names
			results = mainActivity.getSharedPreferences(Match.PREF_KEY_ACCOUNT + account2Name, Context.MODE_PRIVATE).edit();
			results.clear();
			results.apply();

			//remove match results
			results = mainActivity.getSharedPreferences(Match.PREF_KEY_MATCH + accountsKey, Context.MODE_PRIVATE).edit();
			results.clear();
			results.apply();
			
            //creates mime types list for query
            String types = "";
            if (deep) {
                for (String aMIME_TYPE_LIST : MIME_TYPE_LIST) types += "'" + aMIME_TYPE_LIST + "',";
                types = types.substring(0, types.length() - 1);
            } else
                types = "'" + ContactsHelper.TYPE_NAME + "'";

            message = "Starting " + syncType + "...\n";
            message += "Loading Account 1: " + account1Name + "\n";
            publishProgress(message,"0.0");

            ContentResolver mContentResolver = mainActivity.getContentResolver();
            Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
                    .appendQueryParameter(RawContacts.DELETED, "0")
                    .build();

            cursor = mContentResolver.query(
                    rawContactUri,
                    new String[]{RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY},
                    RawContacts.ACCOUNT_NAME + "==? AND " + RawContacts.ACCOUNT_TYPE + "==?",
                    new String[]{account1Name, MainActivity.ACCOUNT_TYPE}, RawContacts.DISPLAY_NAME_PRIMARY);

            numContactsAccount1 = cursor.getCount();

            try {
                while (cursor.moveToNext()) {
					publishProgress("", Float.toString((cursor.getPosition()+1)/numContactsAccount1));
                    if (!cursor.isNull(0) && !cursor.isNull(1)) {
                        tempContactName = cursor.getString(1);
                        tempContactId = cursor.getLong(0);
                        duplicate = false;
                        if (deep) {
                            cItems = mContentResolver.query(Data.CONTENT_URI,
                                    new String[]{Data.RAW_CONTACT_ID, Data.MIMETYPE, Data.DATA1},
                                    Data.RAW_CONTACT_ID + "==? AND " + Data.MIMETYPE + " IN (" + types + ")",
                                    new String[]{Long.toString(tempContactId)}, null);

                            try {
                                while (cItems.moveToNext()) {
                                    if (!cItems.isNull(0) && !cItems.isNull(1) && !cItems.isNull(2)) {
                                        type = cItems.getString(1);
                                        if (type.equals(StructuredName.CONTENT_ITEM_TYPE))
                                            data = tempContactName;
                                        else
                                            data = cItems.getString(2);
                                        tempData = new HashMap<>();
                                        tempData.put(type, cItems.getString(0));

                                        duplicate = performMatchingP1(
                                                account1Other, dup1ListOther,
                                                unmatched1,
                                                unmatched1Id, tempData,
                                                tempContactId,
                                                type, data,
                                                duplicate, false,
                                                null, null
                                        );
                                    }
                                }
                            } finally {
                                cItems.close();
                            }
                        } else {
                            tempData = new HashMap<>();
                            tempData.put(ContactsHelper.TYPE_NAME, tempContactId.toString());

                            duplicate = performMatchingP1(
                                    account1Other, dup1ListOther,
                                    unmatched1,
                                    unmatched1Id, tempData,
                                    tempContactId,
                                    ContactsHelper.TYPE_NAME, tempContactName,
                                    duplicate, false,
                                    null, null
                            );
                        }
                        //store all contacts
                        account1.put(tempContactId, tempContactName);
                        //store all non-duplicates as unmatched
                        if (duplicate)
                            dupCount1++;
                        else {
                            unmatched1.put(tempContactName, tempContactId);
                            unmatched1Id.put(tempContactId, tempContactName);
                            //unmatchedCount1++;
                        }
                    }

                }
            } finally {
                cursor.close();
            }


            message = "Loaded Account 1: "
                    + String.valueOf(numContactsAccount1)
                    + " contacts\n";
            message += "Account 1 Duplicates: " + String.valueOf(dupCount1) + "\n";

            publishProgress(message);

            if (!account2Name.equals(account1Name)) {
				message = "Loading Account 2: " + account2Name + "\n";
				publishProgress(message, "0.0");
				
				// get contracts list from account 2
                cursor = mContentResolver.query(
                        rawContactUri,
                        new String[]{RawContacts._ID, RawContacts.DISPLAY_NAME_PRIMARY},
                        RawContacts.ACCOUNT_NAME + "==? AND " + RawContacts.ACCOUNT_TYPE + "==?",
                        new String[]{account2Name, MainActivity.ACCOUNT_TYPE}, RawContacts.DISPLAY_NAME_PRIMARY);

                numContactsAccount2 = cursor.getCount();

                try {
                    while (cursor.moveToNext()) {
						publishProgress("", Float.toString((cursor.getPosition()+1)/numContactsAccount2));
                        if (!cursor.isNull(0) && !cursor.isNull(1)) {
                            tempContactName = cursor.getString(1);
                            tempContactId = cursor.getLong(0);
                            duplicate = false;
                            matched = false;
                            if (deep) {
                                cItems = mContentResolver.query(Data.CONTENT_URI,
                                        new String[]{Data.RAW_CONTACT_ID, Data.MIMETYPE, Data.DATA1},
                                        Data.RAW_CONTACT_ID + "==? AND " + Data.MIMETYPE + " IN (" + types + ")",
                                        new String[]{Long.toString(tempContactId)}, null);

                                try {
                                    while (cItems.moveToNext()) {
                                        if (!cItems.isNull(0) && !cItems.isNull(1) && !cItems.isNull(2)) {
                                            type = cItems.getString(1);
                                            if (type.equals(StructuredName.CONTENT_ITEM_TYPE))
                                                data = tempContactName;
                                            else
                                                data = cItems.getString(2);
                                            tempData = new HashMap<>();
                                            tempData.put(type, cItems.getString(0));

                                            Boolean returnValues[] = performMatchingP2(
                                                    account1Other, account2Other,
                                                    dup2ListOther,
                                                    unmatched1Id, unmatched2Id,
                                                    unmatched2,
                                                    matched1Other, matched2Other,
                                                    tempData,
                                                    tempContactId,
                                                    type, data,
                                                    duplicate, matched
                                            );

                                            duplicate = returnValues[0];
                                            matched = returnValues[1];
                                        }
                                    }
                                } finally {
                                    cItems.close();
                                }
                            } else {
                                tempData = new HashMap<>();
                                tempData.put(ContactsHelper.TYPE_NAME, tempContactId.toString());

                                Boolean returnValues[] = performMatchingP2(
                                        account1Other, account2Other,
                                        dup2ListOther,
                                        unmatched1Id, unmatched2Id,
                                        unmatched2,
                                        matched1Other, matched2Other,
                                        tempData,
                                        tempContactId,
                                        ContactsHelper.TYPE_NAME, tempContactName,
                                        duplicate, matched
                                );

                                duplicate = returnValues[0];
                                matched = returnValues[1];
                            }
                            //store all contacts
                            account2.put(tempContactId, tempContactName);
                            //store all non-duplicates as unmatched
                            if (!duplicate && !matched) {
                                unmatched2.put(tempContactName, tempContactId);
                                unmatched2Id.put(tempContactId, tempContactName);
                                //unmatchedCount2++;
                            }

                            if (duplicate)
                                dupCount2++;

                            //remove from unmatched1
                            if (matched) {
                                Long account1id = (long) -1;
                                Boolean nameMatch = false;
                                if (matched2Other.containsKey(ContactsHelper.TYPE_NAME)
                                        && matched2Other.get(ContactsHelper.TYPE_NAME).containsKey(tempContactId)) {
                                    account1id = matched2Other.get(ContactsHelper.TYPE_NAME).get(tempContactId);
                                    nameMatch = true;
                                }

                                for (String t : matched2Other.keySet()) {
                                    if (!t.equals(ContactsHelper.TYPE_NAME) && matched2Other.get(t).containsKey(tempContactId)) {
                                        if (nameMatch) {
                                            matched2Other.get(t).remove(tempContactId);
                                            matched1Other.get(t).remove(account1id);
                                        } else
                                            account1id = matched2Other.get(t).get(tempContactId);
                                    }
                                }

                                if (!account1id.equals(Long.valueOf(-1))) {
                                    String a1ContactName = unmatched1Id.remove(account1id);
                                    unmatched1.remove(a1ContactName);
                                    //unmatchedCount1--;
                                }
                            }
                        }

                    }
                } finally {
                    cursor.close();
                }

                message = "Loaded Account 2: "
                        + String.valueOf(numContactsAccount2)
                        + " contacts\n";
                message += "Account 2 Duplicates: " + String.valueOf(dupCount2) + "\n";
                publishProgress(message);

                int matches = 0;
                for (String mime : MIME_TYPE_LIST)
                    if (!mime.equals(ContactsHelper.TYPE_NAME))
                        if (matched1Other.get(mime) != null)
                            matches += matched1Other.get(mime).size();


                message = "Results:\n";
                message += "Matched: " + String.valueOf(matched1Other.get(ContactsHelper.TYPE_NAME).size()) + "\n";
                message += "Potential Matches: " + String.valueOf(matches) + "\n";
                message += "Unmatched from account 1: " + String.valueOf(unmatched1.size()) + "\n";
                message += "Unmatched from account 2: " + String.valueOf(unmatched2.size()) + "\n";
            }

            results = mainActivity.getSharedPreferences(PREF_KEY_ACCOUNT+account1Name, Context.MODE_PRIVATE).edit();

            //store the number of contacts for account1 so that can display results even if no contacts
            results.putInt(NUMCONTACTS + account1Name, numContactsAccount1);
            results.apply();

            HashMap<String, HashSet<String>> dup1Name = new HashMap<>();
            for (String mime : MIME_TYPE_LIST)
                dup1Name.put(mime, new HashSet<String>());
            for (Map.Entry<String, HashMap<String, String>> e : dup1ListOther.entrySet()) {
                for (Map.Entry<String, String> v : e.getValue().entrySet()) {
                    dup1Name.get(v.getKey()).add(e.getKey() + ":" + v.getValue());
                }
            }
            for (String mime : MIME_TYPE_LIST)
                results.putStringSet(DUPKEY + mime + account1Name, dup1Name.get(mime));
            results.apply();

            HashSet<String> account1Set = new HashSet<>();
            for (Map.Entry<Long, String> e : account1.entrySet()) {
                account1Set.add(String.valueOf(e.getKey()) + ":" + e.getValue());
            }
            results.putStringSet(ACCOUNTKEY + account1Name, account1Set);
            results.apply();

            if (!account2Name.equals(account1Name)) {
                results = mainActivity.getSharedPreferences(PREF_KEY_ACCOUNT+account2Name, Context.MODE_PRIVATE).edit();
                //store the number of contacts for account2 so that can display results even if no contacts
                results.putInt(NUMCONTACTS + account2Name, numContactsAccount2);
                results.apply();

                HashMap<String, HashSet<String>> dup2Name = new HashMap<>();
                for (String mime : MIME_TYPE_LIST)
                    dup2Name.put(mime, new HashSet<String>());
                for (Map.Entry<String, HashMap<String, String>> e : dup2ListOther.entrySet()) {
                    for (Map.Entry<String, String> v : e.getValue().entrySet()) {
                        dup2Name.get(v.getKey()).add(e.getKey() + ":" + v.getValue());
                    }
                }
                for (String mime : MIME_TYPE_LIST)
                    results.putStringSet(DUPKEY + mime + account2Name, dup2Name.get(mime));
                results.apply();

                HashSet<String> account2Set = new HashSet<>();
                for (Map.Entry<Long, String> e : account2.entrySet()) {
                    account2Set.add(String.valueOf(e.getKey()) + ":" + e.getValue());
                }
                results.putStringSet(ACCOUNTKEY + account2Name, account2Set);
                results.apply();

                results = mainActivity.getSharedPreferences(PREF_KEY_MATCH+accountsKey, Context.MODE_PRIVATE).edit();

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

                HashMap<String, HashSet<String>> matched1Name = new HashMap<>();
                for (String mime : MIME_TYPE_LIST)
                    matched1Name.put(mime, new HashSet<String>());
                for (Map.Entry<String, HashMap<Long, Long>> e : matched1Other.entrySet()) {
                    for (Map.Entry<Long, Long> v : e.getValue().entrySet()) {
                        matched1Name.get(e.getKey()).add(v.getKey() + ":" + v.getValue());
                    }
                }
                for (String mime : MIME_TYPE_LIST)
                    if (mime.equals(ContactsHelper.TYPE_NAME))
                        results.putStringSet(MATCHEDKEY + account1Name + ":" + account2Name, matched1Name.get(mime));
                    else
                        results.putStringSet(MATCHEDKEY + mime + account1Name + ":" + account2Name, matched1Name.get(mime));
                results.apply();

                HashMap<String, HashSet<String>> matched2Name = new HashMap<>();
                for (String mime : MIME_TYPE_LIST)
                    matched2Name.put(mime, new HashSet<String>());
                for (Map.Entry<String, HashMap<Long, Long>> e : matched2Other.entrySet()) {
                    for (Map.Entry<Long, Long> v : e.getValue().entrySet()) {
                        matched2Name.get(e.getKey()).add(v.getKey() + ":" + v.getValue());
                    }
                }
                for (String mime : MIME_TYPE_LIST)
                    if (mime.equals(ContactsHelper.TYPE_NAME))
                        results.putStringSet(MATCHEDKEY + account2Name + ":" + account1Name, matched2Name.get(mime));
                    else
                        results.putStringSet(MATCHEDKEY + mime + account2Name + ":" + account1Name, matched2Name.get(mime));
                results.apply();
            }
			
			results = mainActivity.getSharedPreferences(PREF_KEY_MATCH+accountsKey, Context.MODE_PRIVATE).edit();
            results.putBoolean(SYNCMATCHED, true).apply();

			status.edit().putBoolean(SYNCING, false).apply();
			
            return message;
        }

		@Override
		protected void onPreExecute()
		{
			syncStarted = true;
			
			super.onPreExecute();
		}

        @Override
        protected void onProgressUpdate(String... message) {
			SharedPreferences logPref = mainActivity.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);
			String log = logPref.getString(LOG_TAG,"");
            
			log = message[0] + log;
			logPref.edit().putString(LOG_TAG,log).apply();
			
			if(message.length > 1) {
				float progress = Float.parseFloat(message[1]);
				logPref.edit().putFloat("PROGRESS",progress).apply();
			} else
				logPref.edit().remove("PROGRESS").apply();
			
			status.refresh();
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);

            if (syncMatched) {
                return;
            }
            if (!message.equals("")) {
                SharedPreferences logPref = mainActivity.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);
				String log = logPref.getString(LOG_TAG,"");

				log = message + log;
				logPref.edit().putString(LOG_TAG,log).apply();
				status.refresh();
            }

            syncMatched = true;

            if (!mainActivity.isChangingConfigurations()&&!mainActivity.isRestricted()&&!mainActivity.isFinishing() && !mainActivity.isDestroyed())
                mainActivity.showResults();
        }
    }
}
