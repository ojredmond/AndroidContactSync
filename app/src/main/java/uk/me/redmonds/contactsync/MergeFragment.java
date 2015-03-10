package uk.me.redmonds.contactsync;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
                    } else if(contact.get(ContactsHelper.TYPE_PHOTO) != null
					        && contact.get(ContactsHelper.TYPE_PHOTO).size() != 1) {
                        Toast.makeText(main, "Multiple contact photos not supported", Toast.LENGTH_LONG).show();
                    } else if (cObject.saveMergedContact(contact)) {
                        main.Compare(listType, listItem, selectedName);
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
                    if(type.equals(ContactsHelper.TYPE_PHOTO)) {
                        row.removeView(row.findViewById(R.id.delete_button));
                        ImageView photo = (ImageView)row.findViewById(R.id.value);
                        photo.setImageBitmap(null);
                        row.setTag(null);
                        ViewGroup photoLayout = (ViewGroup)row.getParent();
                        int posPhoto = photoLayout.indexOfChild(row);
                        
                        if (posPhoto == 0 && photoLayout.getChildAt(1).getTag() == null) {
                            layout.removeView(photoLayout);
                        } else if (posPhoto == 1 && photoLayout.getChildAt(0).getTag() == null) {
                            layout.removeView(photoLayout);
                        }
                    } else {
                        layout.removeView(row);
                    }
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
        LinearLayout photoLayout = null;
        LinearLayout.LayoutParams itemParams = 
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
                                      LinearLayout.LayoutParams.WRAP_CONTENT,1);
        
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
                                                                               LinearLayout.LayoutParams.WRAP_CONTENT);
        for (String type : ContactsHelper.TYPES) {
            if (contact.get(type) != null
                    && contact.get(type).size() > 0) {
                Boolean first = true;
                
                for (StringMap item : contact.get(type)) {
					
                    if (photoLayout != null &&
                            !type.equals(ContactsHelper.TYPE_PHOTO) 
                            && photoLayout.getChildCount() > 0) {
                        if((photoLayout.getChildCount() & 1) == 1) {
                            View photoView = LayoutInflater.from(main)
                                    .inflate(R.layout.thumb, layoutContainer, false);
                            ImageView photo = (ImageView)photoView.findViewById(R.id.value);
                            photo.setTag(null);
                            
                            photo.setLayoutParams(itemParams);
                            photoLayout.addView(photoView);
                        }
                        layout.addView(photoLayout,linearParams);
                        photoLayout = null;
                    }
                    //if (item.get("value") == null && !type.equals(ContactsHelper.TYPE_PHOTO))
                        //break;
                    if(first && !type.equals(ContactsHelper.TYPE_PHOTO)) {
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
                            
                            if(photoLayout != null && 
                                (photoLayout.getChildCount()&1)==0) {
                                    layout.addView(photoLayout);
                                    photoLayout = null;
                            }
                            if(photoLayout == null) {
                                photoLayout = new LinearLayout(main);
                                photoLayout.setOrientation(LinearLayout.HORIZONTAL);
                            }
                            photoLayout.addView(deleteLayout);
                            
                        }
                    } else if (item.get("label") == null) {
                        contactValue = (TextView) LayoutInflater.from(main)
                                .inflate(R.layout.list_row_1, layoutContainer, false);
                        contactValue.setText(item.get("value"));
                        contactValue.setTag(item);
                        //hide delete button if only 1 name or group is My Contacts
                        if (!(type.equals(ContactsHelper.TYPE_NAME) && contact.get(type).size() == 1)
								&& !(type.equals(ContactsHelper.TYPE_GROUP) && item.get("value")!= null && item.get("value").equals("My Contacts")))
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
                    
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.END;
                    
                    deleteLayout.addView(deleteButton,params);

                    deleteLayout.setTag(type);
                        
                    //if only 1 name hide delete button
                    if ((type.equals(ContactsHelper.TYPE_NAME) && contact.get(type).size() == 1) 
							|| (type.equals(ContactsHelper.TYPE_GROUP) && item.get("value") != null && item.get("value").equals("My Contacts"))) {
                        assert contactValue != null;
                        layout.addView(contactValue, linearParams);
                    } else if (!type.equals(ContactsHelper.TYPE_PHOTO))
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
