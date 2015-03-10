package uk.me.redmonds.contactsync;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;

/**
 * A Flexibale list adapter that allows each row to specify layout
 * Created by oli on 21/02/15.
 */
class FlexibleListAdapter extends BaseAdapter {
    public final static String TEXT = "text";
    public final static String TITLE = "Title";
    public final static String DESCRIPTION = "Desc";
    public final static String LAYOUT = "Layout";
    public final static String LAYOUTIDS = "Layout IDs";
    public final static String LISTITEM = "List Item";
    private final HashMap<String, Object>[] items;
    private final Activity context;
    private int defaultLayout = android.R.layout.simple_list_item_2;
    private int defaultTitleId = android.R.id.text1;
    private int defaultDescriptionId = android.R.id.text2;

    public FlexibleListAdapter(HashMap<String, Object>[] i, Activity a, int layout, int id1, int id2) {
        items = i;
        context = a;
        defaultLayout = layout;
        defaultTitleId = id1;
        defaultDescriptionId = id2;
    }

    public FlexibleListAdapter(HashMap<String, Object>[] i, Activity a) {
        items = i;
        context = a;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public HashMap<String, Object> getItem(int i) {
        return items[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (items[i].containsKey(LAYOUT))
                view = mInflater.inflate((int) items[i].get(LAYOUT), null);
            else
                view = mInflater.inflate(defaultLayout, null);

            if (items[i].containsKey(TEXT) && items[i].containsKey(LAYOUTIDS)) {
                String text[] = (String[]) items[i].get(TEXT);
                int ids[] = (int[]) items[i].get(LAYOUTIDS);
                if (text.length == ids.length)
                    for (int y = 0; y < text.length; y++)
                        ((TextView) view.findViewById(ids[y])).setText(text[y]);
            }
            if (items[i].containsKey(TITLE))
                ((TextView) view.findViewById(defaultTitleId)).setText((String) items[i].get(TITLE));
            if (items[i].containsKey(DESCRIPTION))
                ((TextView) view.findViewById(defaultDescriptionId)).setText((String) items[i].get(DESCRIPTION));
        }
        return view;
    }
}

