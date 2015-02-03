package uk.me.redmonds.contactsync;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.*;
import android.widget.TabHost.TabContentFactory;
import android.view.*;
import android.util.*;

public class CompareFragment extends android.app.Fragment
{
    private FragmentActivity main;
    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    private TabHost mTabsHost;
    private int screenWidth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        main = getActivity();
        screenWidth = main.getWindowManager().getDefaultDisplay().getWidth();
        mTabsHost = (TabHost)inflater.inflate(R.layout.tabs, container, false);
        mTabsHost.setup();
        mViewPager = (ViewPager)mTabsHost.findViewById(R.id.viewpager);

        Bundle args = getArguments();
        String listItem = args.getString("listItem");
        String selected = args.getString("selected");

        //main.hideMenuIcon();
        //main.getActionBar().hide();

        mTabsAdapter = new TabsAdapter(main, mViewPager, mTabsHost, screenWidth);
        SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
        StringList list = new StringList(pref, listItem);
        final TabWidget tabWidget = (TabWidget)((HorizontalScrollView)((LinearLayout)mTabsHost.getChildAt(0)).getChildAt(0)).getChildAt(0);

        if (!list.equals(null)) {
            SparseArray<String> contacts = list.getSparseArray();
            for (int i=0; i<contacts.size(); i++)
            {
                String name = contacts.valueAt(i);
                int id = contacts.keyAt(i);
                Bundle argsDetail = new Bundle();
                argsDetail.putString("listItem", listItem);
                argsDetail.putString("name", name);
                argsDetail.putInt("id", id);

                TabHost.TabSpec tab = mTabsHost.newTabSpec(name).setIndicator(name);

                if (listItem.startsWith(Match.UNMATCHNAMEKEY))
                    mTabsAdapter.addTab(tab, MatchContact.class, argsDetail);
                else
                    mTabsAdapter.addTab(tab, CompareDetail.class, argsDetail);

                //set the text colour of the tab
                final TextView tv = (TextView) tabWidget.getChildAt(mTabsAdapter.getCount()-1).findViewById(android.R.id.title);
                //tv.setTextColor(this.getResources().getColorStateList(R.color.));
                tv.setFocusableInTouchMode(true);

                if (name.equals(selected))
                    mTabsHost.setCurrentTab(mTabsAdapter.getCount()-1);
            }
        }

        if (savedInstanceState != null) {
            mTabsAdapter.onPageSelected(savedInstanceState.getInt("tab", 0));
        }

        return mTabsHost;
    }

    @Override
    public void onResume() {
        super.onResume();
        View tab = ((TabWidget)((HorizontalScrollView)((LinearLayout)mTabsHost.getChildAt(0)).getChildAt(0)).getChildAt(0)).getChildAt(mTabsHost.getCurrentTab());
        final TextView tv = (TextView) tab.findViewById(android.R.id.title);
        tv.requestFocus();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("tab", mTabsHost.getCurrentTab());
        super.onSaveInstanceState(outState);
    }

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
    public static class TabsAdapter extends FragmentStatePagerAdapter
            implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener
    {
        private final Context mContext;
        private final TabHost mTabsHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        private final int mScreenWidth;

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private final Integer position;

            TabInfo(Class<?> _class, Bundle _args, int pos) {
                clss = _class;
                args = _args;
                position = pos;
            }
        }

        /**
         * A simple factory that returns dummy views to the Tabhost
         * @author mwho
         */
        static final class TabFactory implements TabContentFactory {
            private final Context mContext;

            /**
             * @param context
             */
            public TabFactory(Context context) {
                mContext = context;
            }

            /** (non-Javadoc)
             * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
             */
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }


        public TabsAdapter(FragmentActivity activity, ViewPager pager, TabHost tabs, int width) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabsHost = tabs;
            mTabsHost.setOnTabChangedListener(this);
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
            mScreenWidth = width;
        }

        public void addTab(TabHost.TabSpec tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args, getCount());
            //tab.setTag(info);
            mTabs.add(info);
            tab.setContent(new TabFactory(mContext));
            mTabsHost.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            Fragment f = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            //f.setTag
            return f;
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            mTabsHost.setCurrentTab(position);
        }

        public void onPageScrollStateChanged(int state) {
        }

        public void onTabChanged(String p1)
        {
            mViewPager.setCurrentItem(mTabsHost.getCurrentTab());
            int position = mTabsHost.getCurrentTab();
            View tab = ((TabWidget)((HorizontalScrollView)((LinearLayout)mTabsHost.getChildAt(0)).getChildAt(0)).getChildAt(0)).getChildAt(position);
            int tabWidth = tab.getMeasuredWidth();
            float tabPos = tab.getX() - (mScreenWidth/2) + (tabWidth/2);

            ((HorizontalScrollView)mTabsHost.findViewById(R.id.tabscroll)).smoothScrollTo(Math.round(tabPos), 0);
        }

        public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        }

        public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        }
    }
}
