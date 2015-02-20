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
import android.app.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import android.widget.*;

/**
 * Fragment used for comparison adds tabs to views
 * Created by oli on 31/01/15.
 */
public class CompareFragment extends Fragment {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    TabsAdapter mTabsAdapter;
    ViewPager mViewPager;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		
        mTabsAdapter =
                new TabsAdapter(
                        getChildFragmentManager());

        View tabs = inflater.inflate(R.layout.fragment_pager, container, false);

        mViewPager = (ViewPager) tabs.findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsAdapter);

		//if (savedInstanceState != null) {
		//	mViewPager.setCurrentItem(savedInstanceState.getInt("tab", 0));
		//}
        return mViewPager;
    }

/*    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt("tab", mViewPager.getCurrentItem());
    }
*/
    public class TabsAdapter extends FragmentStatePagerAdapter {
        private ArrayList<HashMap<String,String>> contacts;
        private String listItem;
		Activity main;
        
        public TabsAdapter(FragmentManager fm) {
            super(fm);

            //get data to display
            Bundle args = getArguments();
            listItem = args.getString("listItem");
			
			main = getActivity();
            SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
            contacts = new ArrayList<>();
            
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
			HashSet<String> account1Set = (HashSet<String>)pref.getStringSet(Match.ACCOUNTKEY + settings.getString(MainActivity.ACCOUNT1, null), null);
			HashMap<String,String> account1 = new HashMap<>();
			for(String entry: account1Set) {
				String contact[] = entry.split(":");
				account1.put(contact[0],contact[1]);
			}
            HashSet<String> set = (HashSet<String>)pref.getStringSet(listItem, null);
            for (String aSet : set) {
				HashMap<String,String> contactsName = new HashMap<>();
                String contact[] = aSet.split(":");
                if (listItem.startsWith(Match.MATCHEDKEY)) {
                    String name = account1.get(contact[0]);
                    contactsName.put(contact[0] + "," + contact[1], name);
					contacts.add(contactsName);
                } else {
                    String name = contact[0];
                    contactsName.put(contact[1], name);
					contacts.add(contactsName);
                }
            }
			Collections.sort(contacts, new ListSortMap());
        }

        @Override
        public Fragment getItem(int i) {
            //pass contact information to Fragment
            Bundle argsDetail = new Bundle();
            argsDetail.putString("listItem", listItem);
            argsDetail.putString("name", (String)contacts.get(i).values().toArray()[0]);
            argsDetail.putString("ids", (String)contacts.get(i).keySet().toArray()[0]);

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
			return (CharSequence)contacts.get(position).values().toArray()[0];
        }
    }
	
	/*@Override
	public void onResume()
	{
		// TODO: Implement this method
		super.onResume();
		Toast.makeText(getActivity(),"test",Toast.LENGTH_SHORT).show();
	}*/
}
