package uk.me.redmonds.contactsync;

import android.content.SharedPreferences;
import android.util.SparseArray;

import java.util.HashSet;
import java.util.Set;

public class StringList
{
    private String listName;
    private Set<String> stringList = null;
    private HashSet<String> namesHash = null;
    private SparseArray<String> namesArray = null;
    SharedPreferences pref;

    StringList (SharedPreferences p, String tag) {
        pref = p;
        listName = tag;
        String name[];
        SparseArray<String> account1Names = null;
        SparseArray<String> account2Names = null;
        namesHash = new HashSet<String>();
        namesArray = new SparseArray<String>();

        stringList = pref.getStringSet(listName, null);

        if (listName.startsWith(Match.MATCHEDKEY)) {
            account1Names = new SparseArray<String>();
            Set<String> accountSet = pref.getStringSet(Match.ACCOUNTKEY + listName.split(":")[1], null);

            for (String s : accountSet) {
                name = s.split(":", 0);
                account1Names.put(Integer.parseInt(name[0]), name[1]);
            }

            account2Names = new SparseArray<String>();
            accountSet = pref.getStringSet(Match.ACCOUNTKEY + listName.split(":")[2], null);

            for (String s : accountSet) {
                name = s.split(":", 0);
                account2Names.put(Integer.parseInt(name[0]), name[1]);
            }
        }

        if (stringList != null) {
            for (String l : stringList) {
                name = l.split(":", 0);
                if (listName.startsWith(Match.MATCHEDKEY)) {
                    namesHash.add(account1Names.get(Integer.parseInt(name[0])));
                    namesArray.put(Integer.parseInt(name[0]), account1Names.get(Integer.parseInt(name[0])));
                    namesHash.add(account2Names.get(Integer.parseInt(name[1])));
                    namesArray.put(Integer.parseInt(name[1]), account2Names.get(Integer.parseInt(name[1])));
                } else {
                    namesHash.add(name[1]);
                    namesArray.put(Integer.parseInt(name[0]), name[1]);
                }

            }
        }
    }

    public HashSet<String> getHashSet () {
        return namesHash;
    }

    public SparseArray<String> getSparseArray () {
        return namesArray;
    }

    public Boolean removeEntry (String id) {
        stringList.remove(id + ":" + namesArray.get(Integer.parseInt(id)));
        SharedPreferences.Editor e = pref.edit();
        e.putStringSet(listName,stringList);
        return true;
    }

    public Boolean addEntry (String id, String name) {
        stringList.add(id + ":" + name);
        SharedPreferences.Editor e = pref.edit();
        e.putStringSet(listName,stringList);
        return e.commit();
    }
}