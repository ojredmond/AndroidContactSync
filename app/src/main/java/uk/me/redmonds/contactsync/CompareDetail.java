package uk.me.redmonds.contactsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CompareDetail extends Fragment {
    private ContactsHelper cObj;
    private MainActivity main;
    private String name;
    private String ids[];
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
                default:
                    Toast.makeText(main, p1.toString(), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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

        LinearLayout layout = (LinearLayout) compareView.findViewById(R.id.compare);

        if (listItem.startsWith(Match.MATCHEDKEY))
            cObj = new ContactsHelper(main, listItem, ids[0], ids);
        else
            cObj = new ContactsHelper(main, listItem, name, ids);

        for (String id : ids) {
            layout.addView(cObj.getContactView(container, name, id));
        }

        return compareView;
    }
}
