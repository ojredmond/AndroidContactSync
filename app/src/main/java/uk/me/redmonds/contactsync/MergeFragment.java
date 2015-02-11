package uk.me.redmonds.contactsync;

import android.app.Fragment;
import android.app.FragmentManager;
import android.view.*;
import android.view.View.OnClickListener;
import android.os.*;
import java.util.*;
import android.widget.*;
import android.content.SharedPreferences;

public class MergeFragment extends Fragment
{
    private ArrayList<String> ids;
    private MainActivity main;
    private HashMap<String,HashSet<HashMap<String,String>>> contact;
    private HashSet<String> contacts;
    private LinearLayout layout;
    private Contacts cObject;
    private String listItem;
    private ViewGroup layoutContainer;
    private String listType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutContainer = container;
        Bundle args = getArguments();
        listType = args.getString("listType");
        listItem = args.getString("listItem");
        ids = args.getStringArrayList("ids");
        main = (MainActivity)this.getActivity();
        //main.showMenuIcon();

        main.setHeading("Merge");

        // get stored contact enables rotate top not lose any changes
        //SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
        //contact = (HashSet<String>)pref.getStringSet("contactMerge", null);
        //contacts = (HashSet<String>)pref.getStringSet("contactsMerge", null);

        //create contacts object
        //StringList l = new StringList(pref, listItem);
        cObject = new Contacts(main, new HashSet<>(ids));

        View contactView = inflater.inflate(R.layout.fragment_merge, container, false);
        // add listeners to buttons
        Button cancel = (Button)contactView.findViewById(R.id.contact_cancel);
        cancel.setOnClickListener(ButtonClick);
        Button confirm = (Button)contactView.findViewById(R.id.contact_confirm);
        confirm.setOnClickListener(ButtonClick);

        layout = (LinearLayout)contactView.findViewById(R.id.contact_info);

        // merge contact if no contact stored
        if (contact == null) {
            contact = cObject.mergeContact();
        }

        displayMergedContact();

        return contactView;
    }

    public void displayMergedContact () {
        for(String type: Contacts.types) {
            if(contact.get(type) != null 
                && contact.get(type).size() > 0) {
                TextView contactHeading = (TextView)LayoutInflater.from(main)
                    .inflate(R.layout.list_heading, layoutContainer, false);
                contactHeading.setText(Contacts.getGroupName(type));
                contactHeading.setTag("Heading");
                layout.addView(contactHeading);
                for(HashMap<String,String> item: contact.get(type)) {
                    TextView contactValue;
                    RelativeLayout deleteLayout = (RelativeLayout)LayoutInflater.from(main)
                        .inflate(R.layout.delete_button, layoutContainer, false);
                    if(item.get("label") == null) {
                        contactValue = (TextView)LayoutInflater.from(main)
                            .inflate(R.layout.list_row_1, layoutContainer, false);
                        contactValue.setText(item.get("data1"));
						contactValue.setTag(item);
                        //if only 1 name hide delete button
                        if(!(type.equals(Contacts.types[0]) && contact.get(type).size() == 1))
                            deleteLayout.addView(contactValue);
                    } else {
                        LinearLayout rowLayout = (LinearLayout)LayoutInflater.from(main)
                            .inflate(R.layout.list_row_2, layoutContainer, false);
						contactValue = (TextView)rowLayout.findViewById(R.id.value);
						contactValue.setText(item.get("data1"));
						contactValue.setTag(item);
                        ((TextView)rowLayout.findViewById(R.id.type)).setText(item.get("label"));
                        deleteLayout.addView(rowLayout);
                    }
                    
                    // listener to delete button
                    deleteLayout.findViewById(R.id.delete_button).setOnClickListener(ButtonClick);
                    
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                    deleteLayout.setTag(type);

                    //if only 1 name hide delete button
                    if(type.equals(Contacts.types[0]) && contact.get(type).size() == 1)
                        layout.addView(contactValue);
                    else
                        layout.addView(deleteLayout, params);
                }
            }
        }
    }

    public void onDestroyView() {
        super.onDestroyView();

        /*contact = new HashSet<String> ();
        View view;
        for (int i=0; i < layout.getChildCount(); i++) {
            view = layout.getChildAt(i);
            if(view.getTag() != null && !view.getTag().equals("")) {
                contact.add(view.getContentDescription()
                        + ";" + ((EditText)view).getText());
            }
        }*/

        //SharedPreferences.Editor pref = main.getPreferences(Context.MODE_PRIVATE).edit();
        //pref.putStringSet("contactMerge", contact);
        //pref.putStringSet("contactsMerge", cObject.getContacts());
        //pref.apply();
    }

    OnClickListener ButtonClick = new OnClickListener() {

        public void onClick(View p1)
        {
            switch (p1.getId()) {
                case R.id.contact_confirm:
                    View view;
                    ArrayList<String[]> contactItems = new ArrayList<>();
                    for (int y=0; y < layout.getChildCount(); y++) {
                        view = layout.getChildAt(y);
                        if(view.getTag() != null && !view.getTag().equals("")) {
                            contactItems.add(new String[] {
                                    (String)view.getContentDescription(),
                                    ((String)view.getContentDescription()).split(";",2)[1],
                                    ((EditText)view).getText().toString()});
                        }
                    }

                    if (cObject.saveMergedContact(contactItems)) {
                        /*FragmentManager fragMan = main.getSupportFragmentManager();
                        if (fragMan.getBackStackEntryCount() > 0) {
                            fragMan.popBackStack();
                        }*/
                        main.Compare(listType,listItem,null);
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
					RelativeLayout row = (RelativeLayout)p1.getParent();
					String type = (String)row.getTag();
					Integer pos = layout.indexOfChild(row);
					HashMap<String,String> item = (HashMap<String,String>)row.findViewById(R.id.value).getTag();

					if(contact.get(type).size() == 1 &&
						type.equals(Contacts.types[0])) {
							Toast.makeText(main, "A name is required!", Toast.LENGTH_SHORT).show();
                            return;
					}
					
					if(!contact.get(type).remove(item)) {
						Toast.makeText(main, "Failed to remove information from contact!", Toast.LENGTH_SHORT).show();
						return;
					}
					
					if(contact.get(type).size() == 0 && pos > 0) {
							layout.removeView(layout.getChildAt(pos-1));
					}
					
					//remove delete button if only one name
					if(contact.get(type).size() == 1 &&
					   type.equals(Contacts.types[0])) {
					   View rowOther;
						if(pos > 0) {
						    rowOther = layout.getChildAt(pos-1);
    						if (!rowOther.getTag().equals("Heading")) {
                                ((ViewGroup)rowOther).removeView(rowOther.findViewById(R.id.delete_button));
                                layout.removeView(rowOther.findViewById(R.id.delete_button));
                            }
						}
						if(pos < layout.getChildCount()) {
						    rowOther = layout.getChildAt(pos+1);
						    if (!rowOther.getTag().equals("Heading"))
                                ((ViewGroup)rowOther).removeView(rowOther.findViewById(R.id.delete_button));
						}
					}
					layout.removeView(row);
            }
        }
    };
}
