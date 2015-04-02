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
import android.content.*;

class Sync {
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
            SharedPreferences prefMatch = mainActivity.getSharedPreferences(Match.PREF_KEY_MATCH+accountsKey, Context.MODE_PRIVATE);
            
            if(!prefMatch.getBoolean(Match.SYNCMATCHED, false))
                return "Sync Matched is false, please perform matching first";
                

            String un1 = Match.UNMATCHNAMEKEY + account1Name + ":" + account2Name;
            String un2 = Match.UNMATCHNAMEKEY + account2Name + ":" + account1Name;
            String md = Match.MATCHEDKEY + account1Name + ":" + account2Name;
            HashSet<String> unmatched1 = (HashSet<String>) prefMatch.getStringSet(un1, null);
            HashSet<String> unmatched2 = (HashSet<String>) prefMatch.getStringSet(un2, null);
            HashSet<String> matched = (HashSet<String>) prefMatch.getStringSet(md, null);

			String message = syncTimeStamp + "\n\n";
            message += "Starting " + syncType + "...\n";
            message += account1Name + " & " + account2Name + "\n";
			int max = matched.size();
            publishProgress(message,"0", String.valueOf(max));
			
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            int index = 0;
            for(String idString: matched) {
				index++;
				ContactsHelper contacts = new ContactsHelper(mainActivity,md,null,idString.split(":"));
				HashMap<String, HashSet<StringMap>> contact = contacts.mergeContact();
				ContactsHelper.removeDuplicateNamePhoto(contact);
				ops.addAll(contacts.saveMergedContactBatch(contact));
                publishProgress(contact.get(ContactsHelper.TYPE_NAME).iterator().next().get("value") + "\n", String.valueOf(index), String.valueOf(max));
            }
            return "Sync complete\n";
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
			status.updateLog(syncTimeStamp, message);
        }

        @Override
        protected void onPostExecute(String message) {
			super.onPostExecute(message);
			
			status.updateLog(syncTimeStamp, new String[]{message});
        }
    }
}
