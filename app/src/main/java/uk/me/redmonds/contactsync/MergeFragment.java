package uk.me.redmonds.contactsync;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;

public class MergeFragment extends Fragment {
    private final static String STATE_CONTACT = "merge_contact";
    private MainActivity main;
    private HashMap<String, HashSet<StringMap>> contact;
    private LinearLayout layout;
    private ContactsHelper cObject;
    private String listItem;
    private ViewGroup layoutContainer;
    private String listType;
	private String selectedName;
    private final OnClickListener ButtonClick = new OnClickListener() {

        public void onClick(View p1) {
            switch (p1.getId()) {
                case R.id.contact_confirm:
					if(contact.get(ContactsHelper.TYPE_NAME).size() != 1) {
						Toast.makeText(main, "Needs to be only 1 name", Toast.LENGTH_LONG).show();
					} else if (cObject.saveMergedContact(contact)) {
                        main.Compare(listType, listItem, null);
                    } else {
                        Toast.makeText(main, "Save Failed", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.contact_cancel:
                    main.Compare(listType, listItem, selectedName);
                    break;
                default:
                    ViewGroup row = (ViewGroup) p1.getParent();
                    String type = (String) row.getTag();
                    Integer pos = layout.indexOfChild(row);
                    StringMap item = (StringMap) p1.getTag();

                    if (contact.get(type).size() == 1 &&
						type.equals(ContactsHelper.TYPE_NAME)) {
                        Toast.makeText(main, "A name is required!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!contact.get(type).remove(item)) {
                        Toast.makeText(main, "Failed to remove information from contact!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (contact.get(type).size() == 0 && pos > 0) {
                        layout.removeView(layout.getChildAt(pos - 1));
                    }

                    //remove delete button if only one name
                    if (contact.get(type).size() == 1 &&
                            type.equals(ContactsHelper.TYPE_NAME)) {
                        View rowOther;
                        if (pos > 0) {
                            rowOther = layout.getChildAt(pos - 1);
                            if (!rowOther.getTag().equals("Heading")) {
                                ((ViewGroup) rowOther).removeView(rowOther.findViewById(R.id.delete_button));
                                layout.removeView(rowOther.findViewById(R.id.delete_button));
                            }
                        }
                        if (pos < layout.getChildCount()) {
                            rowOther = layout.getChildAt(pos + 1);
                            if (!rowOther.getTag().equals("Heading"))
                                ((ViewGroup) rowOther).removeView(rowOther.findViewById(R.id.delete_button));
                        }
                    }
                    layout.removeView(row);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutContainer = container;
        Bundle args = getArguments();
        listType = args.getString("listType");
        listItem = args.getString("listItem");
		selectedName = args.getString("selected");
        String[] ids = args.getStringArray("ids");
        main = (MainActivity) this.getActivity();

        main.setHeading("Merge");

        // get stored contact enables rotate top not lose any changes
        if (savedInstanceState != null)
            contact = (HashMap<String, HashSet<StringMap>>) savedInstanceState.getSerializable(STATE_CONTACT);
        //create contacts object
        cObject = new ContactsHelper(main, listItem, selectedName, ids);

        View contactView = inflater.inflate(R.layout.fragment_merge, container, false);
        // add listeners to buttons
        Button cancel = (Button) contactView.findViewById(R.id.contact_cancel);
        cancel.setOnClickListener(ButtonClick);
        Button confirm = (Button) contactView.findViewById(R.id.contact_confirm);
        confirm.setOnClickListener(ButtonClick);

        layout = (LinearLayout) contactView.findViewById(R.id.contact_info);

        // merge contact if no contact stored
        if (contact == null) {
            contact = cObject.mergeContact();
        }

        displayMergedContact();

        return contactView;
    }

    void displayMergedContact() {
        TableLayout.LayoutParams tableParams = 
            new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 
                                   TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowParams = 
            new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
									  TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams itemParams = 
            new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
                                300,1f);
        
        TableLayout photoLayout = new TableLayout(main);
        photoLayout.setLayoutParams(tableParams);
        TableRow photoRow = null;

		//RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		//params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
		
		LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
																			   LinearLayout.LayoutParams.WRAP_CONTENT,1f);
        for (String type : ContactsHelper.TYPES) {
            if (contact.get(type) != null
                    && contact.get(type).size() > 0) {
                Boolean first = true;
				
                for (StringMap item : contact.get(type)) {
					if (photoRow != null &&
							!type.equals(ContactsHelper.TYPE_PHOTO) 
							&& photoRow.getChildCount() > 0) {
						photoLayout.addView(photoRow);
						layout.addView(photoLayout,linearParams);
						photoRow = null;
					}
					if (item.get("value") == null && !type.equals(ContactsHelper.TYPE_PHOTO))
						break;
					if(first) {
						TextView contactHeading = (TextView) LayoutInflater.from(main)
							.inflate(R.layout.list_heading, layoutContainer, false);
						contactHeading.setText(ContactsHelper.getGroupName(type));
						contactHeading.setTag("Heading");
						layout.addView(contactHeading);
						first = false;
					}
                    TextView contactValue;
                    FrameLayout deleteLayout = new FrameLayout(main);
					contactValue = null;
					
					if (type.equals(ContactsHelper.TYPE_PHOTO)) {
						byte[] photoData = item.getByteArray(ContactsHelper.PHOTO);
						if(photoData != null) {
							View photoView = LayoutInflater.from(main)
								.inflate(R.layout.thumb, layoutContainer, false);
							ImageView photo = (ImageView)photoView.findViewById(R.id.value);
							photo.setTag(item);
							Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoData,0,photoData.length);
							photo.setImageBitmap(photoBitmap);
							deleteLayout.addView(photoView);
							deleteLayout.setLayoutParams(itemParams);
							
							if(photoRow != null && 
									(photoRow.getChildCount()&1)==0) {
							    photoLayout.addView(photoRow);
							    photoRow = null;
							}
							if(photoRow == null) {
									photoRow = new TableRow(main);
									photoRow.setLayoutParams(rowParams);
							}
							
							/*GridLayout.Spec rowSpec = GridLayout.spec(0);
							GridLayout.Spec colSpec = GridLayout.spec(photoLayout.getChildCount());
							photoParams = new GridLayout.LayoutParams(rowSpec,colSpec);
							
							deleteLayout.setLayoutParams(photoParams);*/
						}
					} else if (item.get("label") == null) {
                        contactValue = (TextView) LayoutInflater.from(main)
                                .inflate(R.layout.list_row_1, layoutContainer, false);
                        contactValue.setText(item.get("value"));
                        contactValue.setTag(item);
                        //if only 1 name hide delete button
                        if (!(type.equals(ContactsHelper.TYPE_NAME) && contact.get(type).size() == 1))
                            deleteLayout.addView(contactValue);
                    } else {
                        LinearLayout rowLayout = (LinearLayout) LayoutInflater.from(main)
                                .inflate(R.layout.list_row_2, layoutContainer, false);
                        contactValue = (TextView) rowLayout.findViewById(R.id.value);
                        contactValue.setText(item.get("value"));
                        contactValue.setTag(item);
                        ((TextView) rowLayout.findViewById(R.id.type)).setText(item.get("label"));
                        deleteLayout.addView(rowLayout);
                    }

                    // listener to delete button
					ImageButton deleteButton = (ImageButton) LayoutInflater.from(main)
                            .inflate(R.layout.delete_button, layoutContainer, false);
                    deleteButton.setOnClickListener(ButtonClick);
					deleteButton.setTag(item);
					deleteLayout.addView(deleteButton);

					deleteLayout.setTag(type);
						
                    //if only 1 name hide delete button
                    if (type.equals(ContactsHelper.TYPE_NAME) && contact.get(type).size() == 1) {
                        assert contactValue != null;
                        layout.addView(contactValue, linearParams);
                    } else if (type.equals(ContactsHelper.TYPE_PHOTO)) {
                        assert photoRow != null;
                        photoRow.addView(deleteLayout);
                    } else
                        layout.addView(deleteLayout,linearParams);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_CONTACT, contact);
    }
}
