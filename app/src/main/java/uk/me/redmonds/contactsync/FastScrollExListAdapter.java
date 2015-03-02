import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.content.Context;
import android.widget.AbsListView;
import android.widget.SectionIndexer;

public class FastScrollExListAdapter extends SimpleExpandableListAdapter
                                     implements SectionIndexer, AbsListView.OnScrollListener {
 
    private final ExpandableListView expandableListView;
    private final Context context;
    private boolean manualScroll;
    private String[] groups;
 
    /* 
     *  Your other fields
     *      ...
     */
    
    public FastScrollExListAdapter(Context context, ExpandableListView expandableListView /* Your other arguments */) {
        this.context = context;
        this.expandableListView = expandableListView;
        this.expandableListView.setOnScrollListener(this);
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
        // Provide your custom sections here
        return groups;
    }
 
    /* 
     * This method will get called either if the user scrolls manually or if he scrolls 
     * using the fast scrollbars.
     */
    @Override
    public int getPositionForSection(int section) {
        if (manualScroll) {
            // If we are scrolling manually return only the section
            return section;
        } else {
            // If we are scrolling via fast scrollbars get the packed position from the group(section) and
            // transform it into a flat position
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
