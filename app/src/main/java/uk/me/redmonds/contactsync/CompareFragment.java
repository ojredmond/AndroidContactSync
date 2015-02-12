package uk.me.redmonds.contactsync;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by oli on 31/01/15.
 */
public class CompareFragment extends android.app.Fragment {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    TabsAdapter mTabsAdapter;
    ViewPager mViewPager;
    MainActivity main;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        main = (MainActivity)this.getActivity();
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mTabsAdapter =
                new TabsAdapter(
                        main.getSupportFragmentManager());

        View tabs = inflater.inflate(R.layout.fragment_pager, container, false);

        mViewPager = (ViewPager) tabs.findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsAdapter);

		if (savedInstanceState != null) {
			mViewPager.setCurrentItem(savedInstanceState.getInt("tab", 0));
		}
        return mViewPager;
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("tab", mViewPager.getCurrentItem());
		super.onSaveInstanceState(outState);
	}

    public class TabsAdapter extends FragmentStatePagerAdapter {
        private ArrayList<String> contacts;
        private ArrayList<String> contactsids;
        private String listItem;
        
        public TabsAdapter(FragmentManager fm) {
            super(fm);

            //get data to display
            Bundle args = getArguments();
            listItem = args.getString("listItem");
            String selected = args.getString("selected");
    
            SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
            contacts = new ArrayList<String>();
            contactsids = new ArrayList<String>();
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
			HashSet<String> account1Set = (HashSet<String>)pref.getStringSet(Match.ACCOUNTKEY + settings.getString(MainActivity.ACCOUNT1, null), null);
			HashMap<String,String> account1 = new HashMap<String,String>();
			for(String entry: account1Set) {
				String contact[] = entry.split(":");
				account1.put(contact[0],contact[1]);
			}
            HashSet set = (HashSet<String>)pref.getStringSet(listItem, null);
            Iterator setIt = set.iterator();
            while(setIt.hasNext()) {
                String contact[] = ((String)setIt.next()).split(":");
				if (listItem.startsWith(Match.MATCHEDKEY)){
					contacts.add(account1.get(contact[0]));
					contactsids.add(contact[0] + "," + contact[1]);
				} else {
					contacts.add(contact[0]);
					contactsids.add(contact[1]);
				}  
            }
        }

        @Override
        public Fragment getItem(int i) {
            //pass contact information to Fragment
            Bundle argsDetail = new Bundle();
            argsDetail.putString("listItem", listItem);
            argsDetail.putString("name", contacts.get(i));
            argsDetail.putString("ids", contactsids.get(i));

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
            return contacts.get(position);
        }
    }
}
