package uk.me.redmonds.contactsync;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class CompareDetail extends Fragment {
    private ContactsHelper cObj;
    private MainActivity main;
    private String name;
    private String ids[];
    private LinearLayout layout;
    private String listItem;
    private final OnClickListener ButtonClick = new OnClickListener() {

        public void onClick(View p1) {

            switch (p1.getId()) {
                case R.id.delete_contact:
                    cObj.deleteContacts();

                    //reload comparefragement
                    main.Compare(null, listItem, null);
                    break;
                case R.id.merge_contact:
                    main.Merge(name, ids, listItem);
                    break;
                case R.id.unmatched_contact:
                    cObj.addToUnmatched();

                    //reload comparefragement
                    main.Compare(null, listItem, null);
                    break;
                case R.id.delete_button:
                    HashSet<String> idSet = new HashSet<>();
                    idSet.add((String) p1.getTag());
                    cObj.deleteContacts(idSet);

                    if (cObj.size() > 1)
                        fillLayout();
                    else if (cObj.size() > 0)
                        cObj.addToUnmatched();
                    else
                        //reload comparefragement
                        main.Compare(null, listItem, null);
                    break;
                default:
                    Toast.makeText(main, p1.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };
    private ViewGroup layoutContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutContainer = container;

        View compareView = inflater.inflate(R.layout.compare, container, false);

        Bundle args = getArguments();
        if (args == null) {
            return compareView;
        }

        listItem = args.getString("listItem");
        name = args.getString("name");
        ids = args.getString("ids").split(",");
        main = (MainActivity) this.getActivity();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
        String account1Name = settings.getString(MainActivity.ACCOUNT1, null);

        // add buttons and listener
        LinearLayout buttonBar = (LinearLayout) compareView.findViewById(R.id.button_bar);
        Button bDel = (Button) inflater.inflate(R.layout.button, container, false);
        bDel.setId(R.id.delete_button);
        bDel.setText(R.string.delete_contact);

        Button bMerge = (Button) inflater.inflate(R.layout.button, container, false);
        bMerge.setId(R.id.merge_contact);
        if (listItem.startsWith(Match.MATCHEDKEY)
                && !listItem.startsWith(Match.MATCHEDKEY + account1Name))
            bMerge.setText(R.string.confirm);
        else
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
        if (!listItem.startsWith(Match.MATCHEDKEY + account1Name))
            buttonBar.addView(bMerge, params);
        buttonBar.addView(bUn, params);

        if (listItem.startsWith(Match.DUPKEY))
            main.setHeading(getString(R.string.title_activity_dup));
        else if (listItem.startsWith(Match.MATCHEDKEY))
            main.setHeading(getString(R.string.title_activity_match));

        layout = (LinearLayout) compareView.findViewById(R.id.compare);
        fillLayout();

        return compareView;
    }

    private void fillLayout() {
        layout.removeAllViews();

        if (listItem.startsWith(Match.MATCHEDKEY))
            cObj = new ContactsHelper(main, listItem, ids[0], ids);
        else
            cObj = new ContactsHelper(main, listItem, name, ids);

        HashMap<String, HashMap<String, HashSet<StringMap>>> contacts = cObj.getContacts();
        for (String id : ids) {
            String account = cObj.getAccountName(id);
            // create a new view for the contact
            View contactView = LayoutInflater.from(main)
                    .inflate(R.layout.contact, layoutContainer, false);
            contactView.setTag(account + ":" + id);
            layout.addView(contactView);
            LinearLayout contactInfo = (LinearLayout) contactView.findViewById(R.id.contact_info);

            // Display photo if it exists
            if (ContactsHelper.photoInc) {
                byte[] photoData = cObj.getPhoto(id);
                if (photoData != null) {
                    View photoView = LayoutInflater.from(main)
                            .inflate(R.layout.image, layoutContainer, false);
                    ImageView photo = (ImageView) photoView.findViewById(R.id.photo);
                    Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
                    photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight() / 2);
                    photo.setImageBitmap(photoBitmap);
                    contactInfo.addView(photoView);
                }
            }

            // Display account name
            FrameLayout deleteLayout = new FrameLayout(main);
            deleteLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            View accountInfo = LayoutInflater.from(main)
                    .inflate(R.layout.list_account, layoutContainer, false);
            ((TextView) accountInfo.findViewById(R.id.type)).setText("Account");
            ((TextView) accountInfo.findViewById(R.id.value)).setText(cObj.getAccountName(id));
            //deleteLayout.setBackgroundColor(R.color.nav_background);

            // listener to delete button
            ImageButton deleteButton = (ImageButton) LayoutInflater.from(main)
                    .inflate(R.layout.delete_button, layoutContainer, false);
            deleteButton.setOnClickListener(ButtonClick);
            //store Id in tag
            deleteButton.setTag(id);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.END;

            deleteLayout.addView(accountInfo);
            deleteLayout.addView(deleteButton, params);
            contactInfo.addView(deleteLayout);

            // Display contact id
            accountInfo = LayoutInflater.from(main)
                    .inflate(R.layout.list_row_2, layoutContainer, false);
            ((TextView) accountInfo.findViewById(R.id.type)).setText("ID");
            ((TextView) accountInfo.findViewById(R.id.value)).setText(id);
            contactInfo.addView(accountInfo);

            HashMap<String, HashSet<StringMap>> contact = contacts.get(id);
            for (String type : ContactsHelper.TYPES) {
                if (contact.get(type) != null
                        && contact.get(type).size() > 0) {

                    Boolean first = true;
                    for (StringMap item : contact.get(type)) {
                        if (item.get("value") == null)
                            break;
                        if (first) {
                            TextView contactHeading = (TextView) LayoutInflater.from(main)
                                    .inflate(R.layout.list_heading, layoutContainer, false);
                            contactHeading.setText(ContactsHelper.getGroupName(type));
                            contactInfo.addView(contactHeading);
                            first = false;
                        }
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
    }
}
