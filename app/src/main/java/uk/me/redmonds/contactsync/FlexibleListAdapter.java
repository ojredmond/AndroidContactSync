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
public class FlexibleListAdapter extends BaseAdapter {
    public final static String TITLE = "Title";
    public final static String DESCRIPTION = "Desc";
    public final static String LAYOUT = "Layout";
    public final static String TAG = "Tag";
    private HashMap[] items;
    private Activity context;
    private int defaultLayout;

    public FlexibleListAdapter(HashMap[] i, Activity a, int layout) {
        items = i;
        context = a;
        defaultLayout = layout;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public HashMap getItem(int i) {
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
                view = mInflater.inflate(Integer.getInteger((String) items[i].get(LAYOUT)), null);
            else
                view = mInflater.inflate(defaultLayout, null);

            if (items[i].containsKey(TAG))
                view.setTag(items[i].get(TAG));
            if (items[i].containsKey(TITLE))
                ((TextView) view.findViewById(android.R.id.text1)).setText((String) items[i].get(TITLE));
            if (items[i].containsKey(DESCRIPTION))
                ((TextView) view.findViewById(android.R.id.text2)).setText((String) items[i].get(DESCRIPTION));
        }
        return view;
    }
}

