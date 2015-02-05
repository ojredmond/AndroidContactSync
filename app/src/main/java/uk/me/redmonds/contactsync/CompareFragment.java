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

/**
 * Created by oli on 31/01/15.
 */
public class CompareFragment extends android.app.Fragment {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;
    FragmentActivity main;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        main = (FragmentActivity)this.getActivity();
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =
                new DemoCollectionPagerAdapter(
                        main.getSupportFragmentManager());

        View tabs = inflater.inflate(R.layout.fragment_pager, container, false);

        mViewPager = (ViewPager) tabs.findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);

        return mViewPager;
    }

    public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {
        private SparseArray<String> contacts;
        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);

            //get data to display
            Bundle args = getArguments();
            String listItem = args.getString("listItem");
            String selected = args.getString("selected");
    
            SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
            StringList list = new StringList(pref, listItem);

            //if list not empty add tab for each contact
            if (!list.equals(null)) {
                contacts = list.getSparseArray();
                /*for (int i=0; i<contacts.size(); i++)
                {
                    String name = contacts.valueAt(i);
                    int id = contacts.keyAt(i);
                    Bundle argsDetail = new Bundle();
                    argsDetail.putString("listItem", listItem);
                    argsDetail.putString("name", name);
                    argsDetail.putInt("id", id);
    
                    if (listItem.startsWith(Match.UNMATCHNAMEKEY))
                        mTabsAdapter.addTab(tab, MatchContact.class, argsDetail);
                    else
                        mTabsAdapter.addTab(tab, CompareDetail.class, argsDetail);
    
                    if (name.equals(selected))
                        mTabsHost.setCurrentTab(mTabsAdapter.getCount()-1);
                }*/
            }
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DemoObjectFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return contacts.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

    // Instances of this class are fragments representing a single
// object in our collection.
    public static class DemoObjectFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    android.R.layout.simple_list_item_1, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    Integer.toString(args.getInt(ARG_OBJECT)));
            return rootView;
        }
    }
}
