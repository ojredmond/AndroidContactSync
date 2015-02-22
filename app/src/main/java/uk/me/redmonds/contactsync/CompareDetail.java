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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.xml.datatype.*;

public class CompareDetail extends Fragment {
    Contacts cObj;
    private MainActivity main;
    private String name;
    private String ids[];
    private LinearLayout layout;
    private String listItem;
    private OnClickListener ButtonClick = new OnClickListener() {

        public void onClick(View p1) {

            switch (p1.getId()) {
                case R.id.delete_contact:
                    cObj.deleteContacts();

                    //reload comparefragement
                    main.Compare(null, listItem, null);
                    break;
                case R.id.merge_contact:
					Toast.makeText(main,"test "+ids.length,Toast.LENGTH_LONG).show();
                    main.Merge(name, ids, listItem);
                    break;
                case R.id.unmatched_contact:
                    cObj.addToUnmatched();

                    //reload comparefragement
                    main.Compare(null, listItem, null);
                    break;
                default:
                    Toast.makeText(main, p1.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };
    private ViewGroup layoutContainer;
    private View compareView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutContainer = container;

        compareView = inflater.inflate(R.layout.compare, container, false);

        Bundle args = getArguments();
        if (args == null) {
            return compareView;
        }

        listItem = args.getString("listItem");
        name = args.getString("name");
        ids = args.getString("ids").split(",");
		
        // add buttons and listener
        LinearLayout buttonBar = (LinearLayout) compareView.findViewById(R.id.button_bar);
        Button bDel = (Button) inflater.inflate(R.layout.button, container, false);
        bDel.setId(R.id.delete_button);
        bDel.setText(R.string.delete_contact);

        Button bMerge = (Button) inflater.inflate(R.layout.button, container, false);
        bMerge.setId(R.id.merge_contact);
        bMerge.setText(R.string.merge_contacts);

        Button bUn = (Button) inflater.inflate(R.layout.button, container, false);
        bUn.setId(R.id.unmatched_contact);
        bUn.setText(R.string.add_to_unmatched);

        bDel.setOnClickListener(ButtonClick);
        bMerge.setOnClickListener(ButtonClick);
        bUn.setOnClickListener(ButtonClick);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1);
        buttonBar.addView(bDel, params);
        if (listItem.startsWith(Match.DUPKEY))
            buttonBar.addView(bMerge, params);
        buttonBar.addView(bUn, params);

        main = (MainActivity) this.getActivity();
		Toast.makeText(main,"size " + ids.length,Toast.LENGTH_LONG).show();
        if (listItem.startsWith(Match.DUPKEY))
            main.setHeading(getString(R.string.title_activity_dup));
        else if (listItem.startsWith(Match.MATCHEDKEY))
            main.setHeading(getString(R.string.title_activity_match));

        layout = (LinearLayout) compareView.findViewById(R.id.compare);
        fillLayout();

        return compareView;
    }

    private Boolean fillLayout() {
        layout.removeAllViews();

        cObj = new Contacts(main, listItem, ids);
        HashMap<String, HashMap<String, HashSet<HashMap<String, String>>>> contacts = cObj.getContacts();
        for (String id : ids) {
            String account = cObj.getAccountName(id);
            // create a new view for the contact
            View contactView = LayoutInflater.from(main)
                    .inflate(R.layout.contact, layoutContainer, false);
            contactView.setTag(account + ":" + id);
            layout.addView(contactView);
            LinearLayout contactInfo = (LinearLayout) contactView.findViewById(R.id.contact_info);

            // Display account name
			RelativeLayout deleteLayout = (RelativeLayout)LayoutInflater.from(main)
				.inflate(R.layout.delete_button, layoutContainer, false);
            View accountInfo = LayoutInflater.from(main)
                    .inflate(R.layout.list_row_2, layoutContainer, false);
            ((TextView) accountInfo.findViewById(R.id.type)).setText("Account");
            ((TextView) accountInfo.findViewById(R.id.value)).setText(cObj.getAccountName(id));
			deleteLayout.setBackgroundColor(R.color.nav_background);
			
            // listener to delete button
            deleteLayout.findViewById(R.id.delete_button).setOnClickListener(ButtonClick);
			

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
			deleteLayout.addView(accountInfo, params);
            contactInfo.addView(deleteLayout, params);

            // Display contact id
            accountInfo = LayoutInflater.from(main)
                    .inflate(R.layout.list_row_2, layoutContainer, false);
            ((TextView) accountInfo.findViewById(R.id.type)).setText("ID");
            ((TextView) accountInfo.findViewById(R.id.value)).setText(id);
            contactInfo.addView(accountInfo);

            HashMap<String, HashSet<HashMap<String, String>>> contact = contacts.get(id);
            for (String type : Contacts.TYPES) {
                if (contact.get(type) != null
                        && contact.get(type).size() > 0) {
                    TextView contactHeading = (TextView) LayoutInflater.from(main)
                            .inflate(R.layout.list_heading, layoutContainer, false);
                    contactHeading.setText(Contacts.getGroupName(type));
                    contactInfo.addView(contactHeading);
                    for (HashMap<String, String> item : contact.get(type)) {
                        if (item.get("label") == null) {
                            TextView contactValue = (TextView) LayoutInflater.from(main)
                                    .inflate(R.layout.list_row_1, layoutContainer, false);
                            contactValue.setText(item.get("value"));
                            contactInfo.addView(contactValue);
                        } else {
                            LinearLayout rowLayout = (LinearLayout) LayoutInflater.from(main)
                                    .inflate(R.layout.list_row_2, layoutContainer, false);
                            ((TextView) rowLayout.findViewById(R.id.value)).setText(item.get("value"));
                            ((TextView) rowLayout.findViewById(R.id.type)).setText(item.get("label"));
                            contactInfo.addView(rowLayout);
                        }
                    }
                }
            }
        }
        return true;
    }
}
