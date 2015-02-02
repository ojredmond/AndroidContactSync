package uk.me.redmonds.contactsync;

import android.app.Fragment;
import android.app.FragmentManager;
import android.view.*;
import android.view.View.OnClickListener;
import android.os.*;
import java.util.*;
import android.widget.*;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.Context;

public class MergeFragment extends Fragment
{
    private String name;
    private ArrayList<String> ids;
    private MainActivity main;
    private HashSet<String> contact;
    private HashSet<String> contacts;
    private RelativeLayout layout;
    private Contacts cObject;
    private static final Integer[] LABEL_VALUES = new Integer[] {1,101,201,301,401,501};
    private String listItem;

    private String listType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        listType = args.getString("listType");
        listItem = args.getString("listItem");
        name = args.getString("name");
        ids = args.getStringArrayList("ids");
        main = (MainActivity)this.getActivity();
        //main.showMenuIcon();

        ActionBar actionBar = main.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.show();
        actionBar.setTitle(name);

        // get stored contact enables rotate top not lose any changes
        SharedPreferences pref = main.getPreferences(Context.MODE_PRIVATE);
        contact = (HashSet<String>)pref.getStringSet("contactMerge", null);
        contacts = (HashSet<String>)pref.getStringSet("contactsMerge", null);

        //create contacts object
        StringList l = new StringList(pref, listItem);
        cObject = new Contacts(main, new HashSet<String>(ids), l);

        View contactView = inflater.inflate(R.layout.contact, container, false);
        // add listeners to buttons
        ButtonTouch buttonTouch = new ButtonTouch();
        Button cancel = (Button)contactView.findViewById(R.id.contact_cancel);
        cancel.setOnTouchListener(buttonTouch);
        cancel.setOnClickListener(ButtonClick);
        Button confirm = (Button)contactView.findViewById(R.id.contact_confirm);
        confirm.setOnTouchListener(buttonTouch);
        confirm.setOnClickListener(ButtonClick);

        layout = (RelativeLayout)contactView.findViewById(R.id.contact);

        // merge contact if no contact stored
        if (contact == null) {
            contact = cObject.mergeContact();
        }

        displayMergedContact();

        return contactView;
    }

    public void displayMergedContact () {
        ArrayList<String>[] display = new ArrayList[5];
        display[0] = new ArrayList<String> ();
        display[1] = new ArrayList<String> ();
        display[2] = new ArrayList<String> ();
        display[3] = new ArrayList<String> ();
        display[4] = new ArrayList<String> ();
        TextView contactHeading;
        EditText contactDetail;
        ImageButton closeButton;
        View line;
        String label;
        String value;
        int lastId = -1;
        int id;
        RelativeLayout.LayoutParams params;

        // sort contact for display
        for (String s : contact) {
            if (s.startsWith("name")) {
                display[0].add(s);
            } else if (s.startsWith("phone")) {
                display[1].add(s);
            } else if (s.startsWith("email")) {
                display[2].add(s);
            } else if (s.startsWith("postal-address")) {
                display[3].add(s);
            } else {
                display[4].add(s);
            }
        }

        for (int i=0; i < display.length; i++) {
            if (display[i].size() > 0) {
                contactHeading = new TextView(main);
                contactHeading.setId(LABEL_VALUES[i]);
                //contactHeading.setTextColor(getResources().getColor(R.color.pressed_redmond));
                //contactHeading.setTextAppearance(main, R.style.LabelText);
                contactHeading.setTextIsSelectable(true);
                contactHeading.setPadding(10, 10, 10, 10);
                switch (i) {
                    case 0:
                        label = "Name";
                        break;
                    case 1:
                        label = "Number(s)";
                        break;
                    case 2:
                        label = "Email(s)";
                        break;
                    case 3:
                        label = "Address(es)";
                        break;
                    default:
                        label = "Other";
                }
                contactHeading.setText(label);
                params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                if (lastId != -1) {
                    params.addRule(RelativeLayout.BELOW, lastId);
                }

                lastId = LABEL_VALUES[i];
                id = lastId;
                layout.addView(contactHeading, params);

                line = new View(main);
                //line.setBackgroundColor(getResources().getColor(R.color.pressed_redmond));
                params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.height = 2;
                params.addRule(RelativeLayout.BELOW, lastId);
                layout.addView(line, params);
                for (String s : display[i]) {
                    value = s.split(";",3)[2];
                    id++;

                    if (label.equals("Other")) {
                        contactHeading = new TextView(main);
                        contactHeading.setId(id);
                        contactHeading.setTextIsSelectable(true);
                        contactHeading.setPadding(10, 10, 10, 10);
                        contactHeading.setText(s.split(";",2)[0].split("/",2)[0]);

                        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.BELOW, lastId);

                        layout.addView(contactHeading, params);
                        lastId = id;
                        id++;
                    }

                    contactDetail = new EditText(main);
                    contactDetail.setId(id);
                    contactDetail.setText(value);
                    contactDetail.setContentDescription(s.split(";")[0] + ";" + s.split(";")[1]);
                    contactDetail.setTag("detail");

                    params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.BELOW, lastId);

                    layout.addView(contactDetail, params);

                    closeButton = new ImageButton(main);
                    closeButton.setImageResource(android.R.drawable.ic_delete);
                    closeButton.setBackgroundColor(0);
                    params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.BELOW, lastId);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);

                    lastId = id;
                    id++;

                    closeButton.setId(id);
                    closeButton.setOnClickListener(ButtonClick);

                    layout.addView(closeButton, params);
                }
            }
        }
    }

    public void onDestroyView() {
        super.onDestroyView();

        contact = new HashSet<String> ();
        View view;
        for (int i=0; i < layout.getChildCount(); i++) {
            view = layout.getChildAt(i);
            if(view.getTag() != null && !view.getTag().equals("")) {
                contact.add(view.getContentDescription()
                        + ";" + ((EditText)view).getText());
            }
        }

        SharedPreferences.Editor pref = main.getPreferences(Context.MODE_PRIVATE).edit();
        pref.putStringSet("contactMerge", contact);
        pref.putStringSet("contactsMerge", cObject.getContacts());
        pref.commit();
    }

    OnClickListener ButtonClick = new OnClickListener() {

        public void onClick(View p1)
        {
            switch (p1.getId()) {
                case R.id.contact_confirm:
                    View view;
                    ArrayList<String[]> contactItems = new ArrayList<String[]>();
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
                    int id = p1.getId();
                    EditText item = (EditText)layout.findViewById(id-1);

                    if (((String)item.getContentDescription()).startsWith("name")) {
                        View v1 = layout.findViewById(id-3);
                        View v2 = layout.findViewById(id+1);

                        if ((v1 == null || ((String)v1.getContentDescription()).startsWith("name"))
                                && (v2 == null || ((String)v2.getContentDescription()).startsWith("name"))) {
                            Toast.makeText(main, "A name is required!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    contact.remove(item.getContentDescription() + ";" + item.getText().toString());
                    layout.removeAllViews();
                    displayMergedContact();
            }
        }
    };
}