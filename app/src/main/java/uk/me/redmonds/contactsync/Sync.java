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
import java.text.*;
import java.util.*;
import android.app.Activity;

class Sync {
    public static final String LOG_TAG = "SYNC_STATUS";
    Activity mainActivity;
    String syncType;
    StatusFragment status;
    String account1Name, account2Name, accountsKey;
    private String syncTimeStamp;

    public void startSync(MainActivity main, StatusFragment frag, String type) {
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
        
        SyncContacts task = new SyncContacts();
        task.execute();
    }

    private class SyncContacts extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... params) {
            prefMatch = main.getSharedPreferences(Match.PREF_KEY_MATCH+accountsKey, Context.MODE_PRIVATE);
            
            if(prefMatch.getBoolean(Match.SYNCMATCHED, false))
                return "Sync Matched is false, please perform matching first"
                
            String un1 = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
            String un2 = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;
            String md = Match.MATCHEDKEY + account1Name + ":" + account2Name;
            HashSet<String> unmatched1 = (HashSet<String>) prefMatch.getStringSet(un1, null);
            HashSet<String> unmatched2 = (HashSet<String>) prefMatch.getStringSet(un2, null);
            HashSet<String> matched = (HashSet<String>) prefMatch.getStringSet(md, null);

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            for(String idString: matched) {
                onProgressUpdate(idString + "\n");
            }
            return "";
        }

        @Override
        protected void onPreExecute()
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            syncTimeStamp = dateFormat.format(new Date());
            
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... message) {
            SharedPreferences logPref = mainActivity.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);
            String log = logPref.getString(LOG_TAG+syncTimeStamp,"");
            
            log += message[0];
            logPref.edit().putString(LOG_TAG+syncTimeStamp,log).apply();
            
            if(message.length > 1) {
                int progress = Integer.parseInt(message[1]);
                int max = Integer.parseInt(message[2]);
                logPref.edit().putInt("PROGRESS",progress)
                    .putInt("MAX",max)
                    .apply();
            } else
                logPref.edit().remove("PROGRESS").apply();
            
            if (message.length > 3) {
                logPref.edit().putString("ACCOUNT",message[3]).apply();
            }
            status.refresh();
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
        }
    }
}
