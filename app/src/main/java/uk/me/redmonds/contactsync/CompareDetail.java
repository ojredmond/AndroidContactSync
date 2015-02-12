package uk.me.redmonds.contactsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CompareDetail extends Fragment {
    private MainActivity main;
    private HashSet<String> selected;
    private String name;
	private String ids[];
    private LinearLayout layout;
    private HashMap<String,String> dupList;
    //private StringList dup;
    private String listItem;
    //private SparseArray<String> dupList;
    private SharedPreferences pref;
    private ViewGroup layoutContainer;
    private View compareView;
	private Integer bDelId;
	private Integer bMergeId;
	private Integer bUnId;
    //private final static String NAME = "Name";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutContainer = container;

        compareView = inflater.inflate(R.layout.compare, container, false);

        // add buttons and listener
		LinearLayout buttonBar = (LinearLayout)compareView.findViewById(R.id.button_bar);
        Button bDel = (Button)inflater.inflate(R.layout.button, container, false);
		bDel.setId(R.id.delete_button);
		bDel.setText(R.string.delete_contact);
		
		Button bMerge = (Button)inflater.inflate(R.layout.button, container, false);
		bMerge.setId(R.id.merge_contact);
		bMerge.setText(R.string.merge_contacts);
		
		Button bUn = (Button)inflater.inflate(R.layout.button, container, false);
		bUn.setId(R.id.unmatched_contact);
		bUn.setText(R.string.add_to_unmatched);
		
        bDel.setOnClickListener(ButtonClick);
        bMerge.setOnClickListener(ButtonClick);
        bUn.setOnClickListener(ButtonClick);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                                1);
		buttonBar.addView(bDel,params);
		buttonBar.addView(bMerge,params);
		buttonBar.addView(bUn,params);

        main = (MainActivity)this.getActivity();
        if (listItem.startsWith(Match.DUPKEY))
            main.setHeading(getString(R.string.title_activity_dup));
        else if (listItem.startsWith(Match.MATCHEDKEY))
            main.setHeading(getString(R.string.title_activity_match));

        Bundle args = getArguments();
        if (args == null) {
            return compareView;
        }
        listItem = args.getString("listItem");
        name = args.getString("name");
		ids = args.getString("ids").split(",");
		
        pref = main.getPreferences(Context.MODE_PRIVATE);
        selected = (HashSet<String>)pref.getStringSet("selected-" + listItem, null);

        layout = (LinearLayout)compareView.findViewById(R.id.compare);
        fillLayout();

        return compareView;
    }

    private Boolean fillLayout () {
        layout.removeAllViews();
		
		Contacts cObj = new Contacts(main, ids);
		HashMap<String,HashMap<String,HashSet<HashMap<String,String>>>> contacts = cObj.getContacts();
		for (String id: ids) {
			String account = cObj.getAccountName(id);
			// create a new view for the contact
			View contactView = LayoutInflater.from(main)
				.inflate(R.layout.contact, layoutContainer, false);
			contactView.setTag(account + ":" + id);
			layout.addView(contactView);
			LinearLayout contactInfo = (LinearLayout) contactView.findViewById(R.id.contact_info);

			// Display account name
			View accountInfo = LayoutInflater.from(main)
				.inflate(R.layout.list_row_2, layoutContainer, false);
			((TextView)accountInfo.findViewById(R.id.type)).setText("Account");
			((TextView)accountInfo.findViewById(R.id.value)).setText(cObj.getAccountName(id));
			accountInfo.findViewById(R.id.row).setBackgroundColor(getResources().getColor(R.color.nav_background));
			contactInfo.addView(accountInfo);

			// Display contact id
			accountInfo = LayoutInflater.from(main)
				.inflate(R.layout.list_row_2, layoutContainer, false);
			((TextView)accountInfo.findViewById(R.id.type)).setText("ID");
			((TextView)accountInfo.findViewById(R.id.value)).setText(id);
			contactInfo.addView(accountInfo);

			HashMap<String,HashSet<HashMap<String,String>>> contact = contacts.get(id);
			for(String type: Contacts.types) {
				if(contact.get(type) != null 
				   && contact.get(type).size() > 0) {
					TextView contactHeading = (TextView)LayoutInflater.from(main)
						.inflate(R.layout.list_heading, layoutContainer, false);
					contactHeading.setText(Contacts.getGroupName(type));
					contactInfo.addView(contactHeading);
					for(HashMap<String,String> item: contact.get(type)) {
						if(item.get("label") == null) {
							TextView contactValue = (TextView)LayoutInflater.from(main)
								.inflate(R.layout.list_row_1, layoutContainer, false);
							contactValue.setText(item.get("data1"));
							contactInfo.addView(contactValue);
						} else {
							LinearLayout rowLayout = (LinearLayout)LayoutInflater.from(main)
								.inflate(R.layout.list_row_2, layoutContainer, false);
							((TextView)rowLayout.findViewById(R.id.value)).setText(item.get("data1"));
							((TextView)rowLayout.findViewById(R.id.type)).setText(item.get("label"));
							contactInfo.addView(rowLayout);
						}
					}
				}
			}
		}
        return true;
    }

    OnTouchListener ContactTouch = new OnTouchListener() {

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                return false;
            }

            String id = String.valueOf(v.getId());
            if (selected != null && selected.contains(id)) {
                selected.remove(id);
                //v.setBackgroundResource(R.drawable.border);
            } else {
                if (selected == null) {
                    selected = new HashSet<>();
                }
                selected.add(id);
                //v.setBackgroundResource(R.drawable.borderhighlight);
            }

            return false;
        }
    };

    private OnClickListener ButtonClick = new OnClickListener() {

        public void onClick(View p1)
        {
            Contacts contacts;
            ArrayList<String> ids = new ArrayList<>();
            ArrayList<String> accounts = new ArrayList<>();
            for (int i=0; i < layout.getChildCount(); i++) {
                String contact[] = ((String)layout.getChildAt(i).getTag()).split(":");
                ids.add(contact[1]);
                accounts.add(contact[0]);
            }

            switch (p1.getId()) {
                case R.id.delete_contact:
                    if (selected == null) {
                        //add code for none selected
                        Toast.makeText(main, "No contact selected for " + name, Toast.LENGTH_LONG).show();
                        return;
                    }

                    contacts = new Contacts(main, selected);
                    contacts.deleteContacts();

                    //remove duplicate
                    dupList.remove(name);

                    if ((ids.size() - selected.size()) == 1) {
                        for (int i=0; i < ids.size(); i++) {
                            if (!selected.contains(ids.get(i))) {
                                contacts.addToUnmatched(ids.get(i), name, accounts.get(i));
                            }
                        }
                    }

                    fillLayout();
                    break;
                case R.id.merge_contact:
                    main.Merge(name, ids, listItem);
                    break;
                case R.id.unmatched_contact:
                    dupList.remove(name);
                    for (int i=0; i < ids.size(); i++) {
                        contacts = new Contacts(main, new HashSet<>(ids));
                        contacts.addToUnmatched(ids.get(i), name, accounts.get(i));
                    }

                    break;
                default:
                    Toast.makeText(main, p1.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };
}
