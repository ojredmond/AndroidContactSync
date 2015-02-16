package uk.me.redmonds.contactsync;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MergeFragment extends Fragment
{
    private final static String STATE_CONTACT = "merge_contact";
	private ArrayList<String> ids;
    private MainActivity main;
    private HashMap<String,HashSet<HashMap<String,String>>> contact;
    private LinearLayout layout;
    private Contacts cObject;
    private String listItem;
    private ViewGroup layoutContainer;
    private String listType;
    OnClickListener ButtonClick = new OnClickListener() {

        public void onClick(View p1) {
            switch (p1.getId()) {
                case R.id.contact_confirm:
                    if (cObject.saveMergedContact(contact)) {
                        main.Compare(listType, listItem, null);
                    } else {
                        Toast.makeText(main, "Save Failed", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.contact_cancel:
                    FragmentManager fragMan = main.getFragmentManager();
                    if (fragMan.getBackStackEntryCount() > 0) {
                        fragMan.popBackStack();
                    }
                    break;
                default:
                    RelativeLayout row = (RelativeLayout) p1.getParent();
                    String type = (String) row.getTag();
                    Integer pos = layout.indexOfChild(row);
                    HashMap<String, String> item = (HashMap<String, String>) row.findViewById(R.id.value).getTag();

                    if (contact.get(type).size() == 1 &&
                            type.equals(Contacts.TYPES[0])) {
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
                            type.equals(Contacts.TYPES[0])) {
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
        ids = args.getStringArrayList("ids");
        main = (MainActivity) this.getActivity();

        main.setHeading("Merge");

        // get stored contact enables rotate top not lose any changes
        if (savedInstanceState != null)
            contact = (HashMap<String, HashSet<HashMap<String, String>>>) savedInstanceState.getSerializable(STATE_CONTACT);
        //create contacts object
        cObject = new Contacts(main, new HashSet<>(ids));

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
        for (String type : Contacts.TYPES) {
            if (contact.get(type) != null
                    && contact.get(type).size() > 0) {
                TextView contactHeading = (TextView) LayoutInflater.from(main)
                        .inflate(R.layout.list_heading, layoutContainer, false);
                contactHeading.setText(Contacts.getGroupName(type));
                contactHeading.setTag("Heading");
                layout.addView(contactHeading);
                for (HashMap<String, String> item : contact.get(type)) {
                    TextView contactValue;
                    RelativeLayout deleteLayout = (RelativeLayout) LayoutInflater.from(main)
                            .inflate(R.layout.delete_button, layoutContainer, false);
                    if (item.get("label") == null) {
                        contactValue = (TextView) LayoutInflater.from(main)
                                .inflate(R.layout.list_row_1, layoutContainer, false);
                        contactValue.setText(item.get("data1"));
                        contactValue.setTag(item);
                        //if only 1 name hide delete button
                        if (!(type.equals(Contacts.TYPES[0]) && contact.get(type).size() == 1))
                            deleteLayout.addView(contactValue);
                    } else {
                        LinearLayout rowLayout = (LinearLayout) LayoutInflater.from(main)
                                .inflate(R.layout.list_row_2, layoutContainer, false);
                        contactValue = (TextView) rowLayout.findViewById(R.id.value);
                        contactValue.setText(item.get("data1"));
                        contactValue.setTag(item);
                        ((TextView) rowLayout.findViewById(R.id.type)).setText(item.get("label"));
                        deleteLayout.addView(rowLayout);
                    }

                    // listener to delete button
                    deleteLayout.findViewById(R.id.delete_button).setOnClickListener(ButtonClick);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                    deleteLayout.setTag(type);

                    //if only 1 name hide delete button
                    if (type.equals(Contacts.TYPES[0]) && contact.get(type).size() == 1)
                        layout.addView(contactValue);
                    else
                        layout.addView(deleteLayout, params);
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
