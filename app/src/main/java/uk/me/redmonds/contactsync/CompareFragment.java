package uk.me.redmonds.contactsync;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.SparseArray;
import android.content.SharedPreferences;
import android.content.Context;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Created by oli on 31/01/15.
 */
public class CompareFragment extends android.app.Fragment {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    TabsAdapter mTabsAdapter;
    ViewPager mViewPager;
    FragmentActivity main;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        main = (FragmentActivity)this.getActivity();
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
        private String listItem;
        
        public TabsAdapter(FragmentManager fm) {
            super(fm);

            //get data to display
            Bundle args = getArguments();
            listItem = args.getString("listItem");
            String selected = args.getString("selected");
    
            SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
            contacts = new ArraryList<String>();
            HashSet set = (HashSet<String>)pref.getStringSet(listItem, null);
            Iterator dupIt = set.iterator();
            while(dupIt.hasNext()) {
                contacts.add(dupIt.next());
            }

            /*StringList list = new StringList(pref, listItem);

            //if list not empty add tab for each contact
            if (!list.equals(null)) {
                contacts = list.getSparseArray();
            }*/
        }

        @Override
        public Fragment getItem(int i) {
            //pass contact information to Fragment
            Bundle argsDetail = new Bundle();
            argsDetail.putString("listItem", listItem);
            argsDetail.putString("name", contacts.get(i));

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
