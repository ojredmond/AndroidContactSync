package uk.me.redmonds.contactsync;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import android.widget.*;
import java.util.*;
import android.widget.NumberPicker.*;

public class AlphabetListAdapter extends BaseAdapter {

	private static final int TIMES_OVERFLOW = 2;
    private static float sideIndexX;
    private static float sideIndexY;
    private final List<Row> rows;
    private final int alphabetListLayout;
    private final int listLayout;
    private final int itemLayout = R.layout.row_item;
    private final Context context;
    private final View container;
    private final List<Object[]> alphabet = new ArrayList<>();
    private final HashMap<String, Integer> sections = new HashMap<>();
    private int sideIndexHeight;
    private int indexListSize;
	private int height;
	private final int orange_colour;
	private final int default_colour;
	private final ListView listView;
	private final Pattern numberPattern;
	private final List<String> list;

    AlphabetListAdapter(Context c, View v, int lLayout, int aListLayout, ArrayList<String> Objects) {
        context = c;
        container = v;
        listLayout = lLayout;
        alphabetListLayout = aListLayout;
		list = Objects;
        this.rows = new ArrayList<>();
        int start = 0;
        int end;
        String previousLetter = null;
        Object[] tmpIndexItem;
        numberPattern = Pattern.compile("[0-9]");
		listView = (ListView) container.findViewById(listLayout);
		
		orange_colour = context.getResources().getColor(R.color.material_orange_700);
		default_colour = context.getResources().getColor(android.R.color.tertiary_text_light);
		
		listView.setOnScrollListener(new AbsListView.OnScrollListener () {
			public void onScrollStateChanged(AbsListView p1, int p2) {}
			
			public void onScroll(AbsListView p1, int p2, int p3, int p4) {
				highlightLetter();
			}
			
		});
			
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

        //hide alphbet index if to few entries
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view1 = inflater.inflate(itemLayout, (ViewGroup) container, false);
        view1.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int rowHeight = view1.getMeasuredHeight();
		
		//find height of visible objects
		container.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		int containerHeight = container.getMeasuredHeight();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = size.y - containerHeight;

        //Toast.makeText(context,height+"-"+containerHeight+"/"+rowHeight+"="+(height/rowHeight),Toast.LENGTH_LONG).show();
        if (getCount() < ((height / rowHeight) * TIMES_OVERFLOW)) {
            container.findViewById(alphabetListLayout).setVisibility(View.GONE);
        } else {
			updateAlphabetList();
		}
    }

    void updateAlphabetList() {
        LinearLayout sideIndex = (LinearLayout) container.findViewById(alphabetListLayout);
		
        sideIndex.removeAllViews();
        indexListSize = alphabet.size();
        if (indexListSize < 1) {
            return;
        }

		//measure height the letter view
		TextView tmpTV;
		tmpTV = new TextView(context);
		tmpTV.setTextSize(10);
		tmpTV.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		int tmpTVHeight = tmpTV.getMeasuredHeight();
		
		int indexMaxSize = (int) Math.floor(height / tmpTVHeight);
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
		
		//fill the alphabet view
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        for (double i = 1; i <= indexListSize; i = i + delta) {
            Object[] tmpIndexItem = alphabet.get((int) i - 1);
            String tmpLetter = tmpIndexItem[0].toString();
			tmpTV = new TextView(context);
			//tmpTV.setTag(tmpLetter);
			
			if(indexListSize > (int) (i + delta - 2)) {
				String missingLetter = alphabet.get((int) (i + delta - 2))[0].toString();
				if(!tmpLetter.equals(missingLetter))
					tmpLetter += "-" + missingLetter;
			}
				
			tmpTV.setText(tmpLetter);
            tmpTV.setGravity(Gravity.CENTER);
            tmpTV.setTextSize(10);
            tmpTV.setLayoutParams(params);
            sideIndex.addView(tmpTV);
        }

        sideIndexHeight = sideIndex.getHeight();

        final GestureDetector alphabetGestureDetector = new GestureDetector(context, new AlphabetGestureListener());
        sideIndex.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return alphabetGestureDetector.onTouchEvent(event);
            }
        });
    }

    void displayListItem() {
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
            int itemId = R.id.textView1;
            TextView textView = (TextView) view.findViewById(itemId);
            textView.setText(item.text);
        } else { // Section
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                int sectionLayout = R.layout.row_section;
                view = inflater.inflate(sectionLayout, parent, false);
            }

            Section section = (Section) getItem(position);
            int sectionId = R.id.textView1;
            TextView textView = (TextView) view.findViewById(sectionId);
            textView.setText(section.text);
        }

        return view;
    }

    public static abstract class Row {
    }

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

    private class AlphabetGestureListener extends GestureDetector.SimpleOnGestureListener {
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
	
	public void highlightLetter () {
		CharSequence firstLetter;
		HashSet<CharSequence> visibleCharacters = new HashSet<>();

		for(int i=listView.getFirstVisiblePosition(); i<listView.getLastVisiblePosition(); i++) {
			if (getItemViewType(i) == 0)
				firstLetter = ((Item)getItem(i)).text.subSequence(0,1);
			else
				firstLetter = ((Section)getItem(i)).text.subSequence(0,1);

			if (numberPattern.matcher(firstLetter).matches())
				visibleCharacters.add("#");
			else
				visibleCharacters.add(firstLetter);
		}

		LinearLayout sideIndex = (LinearLayout) container.findViewById(alphabetListLayout);

		for(int i=0; i<sideIndex.getChildCount(); i++) {
			Boolean unmatched = true;
			for(CharSequence letter: visibleCharacters)
				if(((String)((TextView)sideIndex.getChildAt(i)).getText()).contains(letter)) {
					((TextView)sideIndex.getChildAt(i)).setTextColor(orange_colour);
					unmatched = false;
				}

			if(unmatched)
				((TextView)sideIndex.getChildAt(i)).setTextColor(default_colour);
		}
	}
}
