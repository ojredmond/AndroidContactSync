package uk.me.redmonds.contactsync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import android.view.*;
import java.util.*;
import java.util.regex.*;
import android.view.View.*;
import android.widget.*;

public class AlphabetListAdapter extends BaseAdapter {

    public class SlideGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            sideIndexX = e2.getX();
			sideIndexY = e2.getY();
			
            if (sideIndexX >= 0 && sideIndexY >= 0) {
                displayListItem();
            }

            return false;
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
			// now you know coordinates of touch
			sideIndexX = e.getX();
			sideIndexY = e.getY();

			// and can display a proper item it country list
			displayListItem();
            return true;
        }
    }

    public static abstract class Row {}
    
    public static final class Section extends Row {
        public final String text;

        public Section(String text) {
            this.text = text;
        }
    }
    
    public static final class Item extends Row {
        public final String text;

        public Item(String text) {
            this.text = text;
        }
    }
    
    private List<Row> rows;
    private int alphabetListLayout;
    private int listLayout;
    private int itemLayout = R.layout.row_item;
    private int itemId = R.id.textView1;
    private int sectionLayout = R.layout.row_section;
    private int sectionId = R.id.textView1;
    private Context context;
    private View container;
    private List<Object[]> alphabet = new ArrayList<Object[]>();
    private HashMap<String, Integer> sections = new HashMap<String, Integer>();
    private int sideIndexHeight;
    private static float sideIndexX;
    private static float sideIndexY;
    private int indexListSize;
    private GestureDetector mGestureDetector;

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    AlphabetListAdapter (Context c, View v, int lLayout, int aListLayout, ArrayList<String> Objects) {
        context = c;
        container = v;
        listLayout = lLayout;
        alphabetListLayout = aListLayout;
        this.rows = new ArrayList<Row>();
        int start = 0;
        int end = 0;
        String previousLetter = null;
        Object[] tmpIndexItem = null;
        Pattern numberPattern = Pattern.compile("[0-9]");

        for (String object : Objects) {
            String firstLetter = object.substring(0, 1).toUpperCase();

            // Group numbers together in the scroller
            if (numberPattern.matcher(firstLetter).matches()) {
                firstLetter = "#";
            }

            // If we've changed to a new letter, add the previous letter to the alphabet scroller
            if (previousLetter != null && !firstLetter.equals(previousLetter)) {
                end = rows.size() - 1;
                tmpIndexItem = new Object[3];
                tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
                tmpIndexItem[1] = start;
                tmpIndexItem[2] = end;
                alphabet.add(tmpIndexItem);

                start = end + 1;
            }

            // Check if we need to add a header row
            if (!firstLetter.equals(previousLetter)) {
                rows.add(new Section(firstLetter));
                sections.put(firstLetter, start);
            }

            // Add the country to the list
            rows.add(new Item(object));
            previousLetter = firstLetter;
        }

        if (previousLetter != null) {
            // Save the last letter
            tmpIndexItem = new Object[3];
            tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
            tmpIndexItem[1] = start;
            tmpIndexItem[2] = rows.size() - 1;
            alphabet.add(tmpIndexItem);
        }
        
        updateAlphabetList();
        
        //hide alphbet index if to few entries
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view1 = inflater.inflate(itemLayout, (ViewGroup)container, false);
        view1.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int rowHeight = view1.getMeasuredHeight();

        container.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int height = container.getMeasuredHeight();

        //Toast.makeText(context,""+height,Toast.LENGTH_LONG).show();
        if(getCount() < ((height/rowHeight) * 2)) {
            container.findViewById(alphabetListLayout).setVisibility(View.GONE);
        }
    }

    public void updateAlphabetList() {
        LinearLayout sideIndex = (LinearLayout) container.findViewById(alphabetListLayout);
        sideIndex.removeAllViews();
        indexListSize = alphabet.size();
        if (indexListSize < 1) {
            return;
        }

        int indexMaxSize = (int) Math.floor(sideIndex.getHeight() / 20);
        int tmpIndexListSize = indexListSize;
        while (tmpIndexListSize > indexMaxSize) {
            tmpIndexListSize = tmpIndexListSize / 2;
        }
        double delta;
        if (tmpIndexListSize > 0) {
            delta = indexListSize / tmpIndexListSize;
        } else {
            delta = 1;
        }

        TextView tmpTV;
        for (double i = 1; i <= indexListSize; i = i + delta) {
            Object[] tmpIndexItem = alphabet.get((int) i - 1);
            String tmpLetter = tmpIndexItem[0].toString();

            tmpTV = new TextView(context);
            tmpTV.setText(tmpLetter);
            tmpTV.setGravity(Gravity.CENTER);
            tmpTV.setTextSize(10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            tmpTV.setLayoutParams(params);
            sideIndex.addView(tmpTV);
        }

        sideIndexHeight = sideIndex.getHeight();

        mGestureDetector = new GestureDetector(context, new SlideGestureListener());
        sideIndex.setOnTouchListener(new OnTouchListener () {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    return mGestureDetector.onTouchEvent(event);
                }
            });
    }

    public void displayListItem() {
        LinearLayout sideIndex = (LinearLayout) container.findViewById(alphabetListLayout);
        sideIndexHeight = sideIndex.getHeight();
        // compute number of pixels for every side index item
        double pixelPerIndexItem = (double) sideIndexHeight / indexListSize;

        // compute the item index for given event position belongs to
        int itemPosition = (int) (sideIndexY / pixelPerIndexItem);

        // get the item (we can do it since we know item index)
        if (itemPosition < alphabet.size()) {
            Object[] indexItem = alphabet.get(itemPosition);
            int subitemPosition = sections.get(indexItem[0]);

            ListView listView = (ListView) container.findViewById(listLayout);
            listView.setSelection(subitemPosition);
        }
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Row getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Section) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        
        if (getItemViewType(position) == 0) { // Item
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(itemLayout, parent, false);  
            }
            
            Item item = (Item) getItem(position);
            TextView textView = (TextView) view.findViewById(itemId);
            textView.setText(item.text);
        } else { // Section
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(sectionLayout, parent, false);  
            }
            
            Section section = (Section) getItem(position);
            TextView textView = (TextView) view.findViewById(sectionId);
            textView.setText(section.text);
        }
        
        return view;
    }
}
