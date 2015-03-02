package uk.me.redmonds.contactsync;

import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.content.Context;
import android.widget.AbsListView;
import android.widget.SectionIndexer;

import java.util.List;
import java.util.Map;
import android.widget.*;

public class FastScrollExListAdapter extends SimpleExpandableListAdapter
                                     implements SectionIndexer, AbsListView.OnScrollListener {
 
    private final ExpandableListView expandableListView;
    private final Context appContext;
    private boolean manualScroll;
    private String[] groups;
 
    public FastScrollExListAdapter(
            Context context, 
            ExpandableListView exListView,
            List<? extends Map<String, ?>> groupData, 
            int groupLayout, String[] groupFrom, 
            int[] groupTo, 
            List<? extends List<? extends Map<String, ?>>> childData, 
            int childLayout, 
            String[] childFrom, 
            int[] childTo) {
    	super(context,groupData,groupLayout,groupFrom,groupTo,childData,childLayout,childFrom,childTo);
    	this.expandableListView = exListView;
        this.expandableListView.setOnScrollListener(this);
    	appContext = context;
    }
    public FastScrollExListAdapter(
            Context context, 
            ExpandableListView exListView,
            List<? extends Map<String, ?>> groupData, 
            int expandedGroupLayout, 
            int collapsedGroupLayout, 
            String[] groupFrom, 
            int[] groupTo, 
            List<? extends List<? extends Map<String, ?>>> childData, 
            int childLayout, 
            String[] childFrom, 
            int[] childTo) {
    	super(context,groupData,expandedGroupLayout,collapsedGroupLayout,groupFrom,groupTo,childData,childLayout,childFrom,childTo);
        this.expandableListView = exListView;
        this.expandableListView.setOnScrollListener(this);
    	appContext = context;
    }
    public FastScrollExListAdapter(
            Context context, 
            ExpandableListView exListView,
            List<? extends Map<String, ?>> groupData,
            int expandedGroupLayout, 
            int collapsedGroupLayout, 
            String[] groupFrom, 
            int[] groupTo, 
            List<? extends List<? extends Map<String, ?>>> childData, 
            int childLayout, 
            int lastChildLayout, 
            String[] childFrom, 
            int[] childTo) {
    	super(context,groupData,expandedGroupLayout,collapsedGroupLayout,groupFrom,groupTo,childData,childLayout,lastChildLayout,childFrom,childTo);
        this.expandableListView = exListView;
        this.expandableListView.setOnScrollListener(this);
    	appContext = context;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.manualScroll = scrollState == SCROLL_STATE_TOUCH_SCROLL;
    }

    @Override
    public void onScroll(AbsListView view, 
                         int firstVisibleItem, 
                         int visibleItemCount, 
                         int totalItemCount) {}

    @Override
    public int getPositionForSection(int section) {
        if (manualScroll) {
            return section;
        } else {            
            return expandableListView.getFlatListPosition(
                       ExpandableListView.getPackedPositionForGroup(section));
        }
    }

    // Gets called when scrolling the list manually
    @Override
    public int getSectionForPosition(int position) {
        return ExpandableListView.getPackedPositionGroup(
                   expandableListView
                       .getExpandableListPosition(position));
    }
}
