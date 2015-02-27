package uk.me.redmonds.contactsync;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;
import android.widget.*;
import android.graphics.*;

public class MergeFragment extends Fragment {
    private final static String STATE_CONTACT = "merge_contact";
    private String[] ids;
    private MainActivity main;
    private HashMap<String, HashSet<StringMap>> contact;
    private LinearLayout layout;
    private ContactsHelper cObject;
    private String listItem;
    private ViewGroup layoutContainer;
    private String listType;
	private String selectedName;
    OnClickListener ButtonClick = new OnClickListener() {

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
                    RelativeLayout row = (RelativeLayout) p1.getParent();
                    String type = (String) row.getTag();
                    Integer pos = layout.indexOfChild(row);
                    StringMap item = (StringMap) row.findViewById(R.id.value).getTag();

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
        ids = args.getStringArray("ids");
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

    public void displayMergedContact() {
        TableLayout.LayoutParams tableParams = 
            new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 
                                   TableLayout.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams rowParams = 
            new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 0, 1f);
        TableRow.LayoutParams itemParams = 
            new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
                                TableRow.LayoutParams.MATCH_PARENT, 1f);
        
        TableLayout photoLayout = new TableLayout(main);
        photoLayout.setLayoutParams(tableParams);
        TableRow photoRow = null;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
		
        for (String type : ContactsHelper.TYPES) {
            if (contact.get(type) != null
                    && contact.get(type).size() > 0) {
                Boolean first = true;
				
                for (StringMap item : contact.get(type)) {
					if (photoLayout != null &&
						!type.equals(ContactsHelper.TYPE_PHOTO) && photoLayout.getChildCount() > 0) {
						    photoLayout.addView(photoRow);
							layout.addView(photoLayout);
							photoLayout = null;
					}
					if (item.get("value") == null && !type.equals(ContactsHelper.TYPE_PHOTO))
						break;
					if(first) {
						TextView contactHeading = (TextView) LayoutInflater.from(main)
							.inflate(R.layout.list_heading, layoutContainer, false);
						contactHeading.setText(ContactsHelper.getGroupName(type));
						layout.addView(contactHeading);
						first = false;
					}
                    TextView contactValue;
                    RelativeLayout deleteLayout = (RelativeLayout) LayoutInflater.from(main)
                            .inflate(R.layout.delete_button, layoutContainer, false);
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
							
							if((photoLayout.getChildCount()&1)==0) {
							    if(photoRow != null)
							        photoLayout.addView(photoRow);
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
                            deleteLayout.addView(contactValue,params);
                    } else {
                        LinearLayout rowLayout = (LinearLayout) LayoutInflater.from(main)
                                .inflate(R.layout.list_row_2, layoutContainer, false);
                        contactValue = (TextView) rowLayout.findViewById(R.id.value);
                        contactValue.setText(item.get("value"));
                        contactValue.setTag(item);
                        ((TextView) rowLayout.findViewById(R.id.type)).setText(item.get("label"));
                        deleteLayout.addView(rowLayout,params);
                    }

                    // listener to delete button
                    deleteLayout.findViewById(R.id.delete_button).setOnClickListener(ButtonClick);

					deleteLayout.setTag(type);
						
                    //if only 1 name hide delete button
                    if (type.equals(ContactsHelper.TYPE_NAME) && contact.get(type).size() == 1)
                        layout.addView(contactValue);
                    else if (type.equals(ContactsHelper.TYPE_PHOTO))
						photoRow.addView(deleteLayout);
					else
						layout.addView(deleteLayout);
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
