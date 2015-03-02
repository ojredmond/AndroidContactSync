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
 
    /* 
     *  Your other fields
     *      ...
     */
    
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
        // toggle the boolean flag to indicate whether the user scrolled
        // manually via touch or used the fast scrollbars
        this.manualScroll = scrollState == SCROLL_STATE_TOUCH_SCROLL;
    }
 
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
 
    @Override
    public String[] getSections() {
		Toast.makeText(appContext,"test",Toast.LENGTH_SHORT).show();
        // Provide your custom sections here
        return groups;
    }
 
    /* 
     * This method will get called either if the user scrolls manually or if he scrolls 
     * using the fast scrollbars.
     */
    @Override
    public int getPositionForSection(int section) {
		Toast.makeText(appContext,"test0",Toast.LENGTH_SHORT).show();
        if (manualScroll) {
            // If we are scrolling manually return only the section
            return section;
        } else {
            // If we are scrolling via fast scrollbars get the packed position from the group(section) and
            // transform it into a flat position
			Toast.makeText(appContext,"test1",Toast.LENGTH_SHORT).show();
            return expandableListView.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(section));
        }
    }
 
    /*
     * This method will only be called if the user scrolls the list manually.
     * After this method #getPositionForSection() will be called
     */
    @Override
    public int getSectionForPosition(int position) {
        // Get the packed position of the provided flat one and find the corresponding goup
        return ExpandableListView.getPackedPositionGroup(expandableListView
                .getExpandableListPosition(position));
    }
    
    /* 
     *  Your adapter logic here
     *     ...
     */
 
}
