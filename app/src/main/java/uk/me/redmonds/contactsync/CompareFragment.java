package uk.me.redmonds.contactsync;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Fragment used for comparison adds tabs to views
 * Created by oli on 31/01/15.
 */
public class CompareFragment extends Fragment {
    private Activity main;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        main = getActivity();

        TabsAdapter mTabsAdapter = new TabsAdapter(
                getFragmentManager());

        View tabs = inflater.inflate(R.layout.fragment_pager, container, false);

        ViewPager mViewPager = (ViewPager) tabs.findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.setCurrentItem(mTabsAdapter.selectedIndex);

        return mViewPager;
    }

    public class TabsAdapter extends FragmentStatePagerAdapter {
        private final ArrayList<HashMap<String, String>> contacts;
        private final String listItem;
        public int selectedIndex = 0;

        public TabsAdapter(FragmentManager fm) {
            super(fm);

            //get data to display
            Bundle args = getArguments();
            listItem = args.getString("listItem");
            String selected = args.getString("selected");
            HashMap selectedMap = null;

            
            contacts = new ArrayList<>();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
			String account1Name = settings.getString(MainActivity.ACCOUNT1, null);
			String account2Name = settings.getString(MainActivity.ACCOUNT2, null);
			String accountsKey;
			if(account1Name.compareTo(account2Name) > 0)
				accountsKey = account1Name + account2Name;
			else
				accountsKey = account2Name + account1Name;
			
			SharedPreferences prefAccount = main.getSharedPreferences(Match.PREF_KEY_ACCOUNT+account1Name, Context.MODE_PRIVATE);
			
            HashSet<String> account1Set = (HashSet<String>) prefAccount.getStringSet(Match.ACCOUNTKEY + account1Name, null);
            HashMap<String, String> account1 = new HashMap<>();
            for (String entry : account1Set) {
                String contact[] = entry.split(":");
                account1.put(contact[0], contact[1]);
            }
			
			SharedPreferences pref;
			if(listItem.startsWith(Match.DUPKEY) && listItem.endsWith(account1Name))
				pref = main.getSharedPreferences(Match.PREF_KEY_ACCOUNT+account1Name, Context.MODE_PRIVATE);
			else if(listItem.startsWith(Match.DUPKEY) && listItem.endsWith(account2Name))
				pref = main.getSharedPreferences(Match.PREF_KEY_ACCOUNT+account2Name, Context.MODE_PRIVATE);
			else
				pref = main.getSharedPreferences(Match.PREF_KEY_MATCH+accountsKey, Context.MODE_PRIVATE);
			
            HashSet<String> set = (HashSet<String>) pref.getStringSet(listItem, null);
            if (set == null || set.size() == 0) {
                main.onBackPressed();
                return;
            }
            for (String aSet : set) {
                HashMap<String, String> contactsName = new HashMap<>();
                String contact[] = aSet.split(":");
                String name;
                if (listItem.startsWith(Match.MATCHEDKEY)) {
                    name = account1.get(contact[0]);
                    contactsName.put(contact[0] + "," + contact[1], name);
                    contacts.add(contactsName);
                } else {
                    name = contact[0];
                    contactsName.put(contact[1], name);
                    contacts.add(contactsName);
                }
                if (selected != null && name.equals(selected))
                    selectedMap = contactsName;
            }
            Collections.sort(contacts, new ListSortMap());
            if (selectedMap != null) {
                selectedIndex = contacts.indexOf(selectedMap);
            }
        }

        @Override
        public Fragment getItem(int i) {
            //pass contact information to Fragment
            Bundle argsDetail = new Bundle();
            argsDetail.putString("listItem", listItem);
            argsDetail.putString("name", (String) contacts.get(i).values().toArray()[0]);
            argsDetail.putString("ids", (String) contacts.get(i).keySet().toArray()[0]);


            Fragment fragment;
            if (listItem.startsWith(Match.UNMATCHNAMEKEY))
                fragment = new MatchContact();
            else
                fragment = new CompareDetail();

            fragment.setArguments(argsDetail);
            return fragment;
        }

        @Override
        public int getCount() {
            return contacts.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return (CharSequence) contacts.get(position).values().toArray()[0];
        }
    }
}
