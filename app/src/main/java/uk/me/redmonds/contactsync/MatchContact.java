package uk.me.redmonds.contactsync;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class MatchContact extends Fragment
        implements OnClickListener, OnItemClickListener {
    private MainActivity main;
    private String id;
    private HashMap<String, String> unmatchedList;
    private HashMap<String, String> matchedList;
    private String listItem;
    private String name;

    @Override
    public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
        if (p1.getAdapter().getItemViewType(p3) == 0) {
            //Toast.makeText(main, "yes",Toast.LENGTH_LONG).show();

            String linkName = (String) ((TextView) p2).getText();
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            //unmatched list
            if (((View) p2.getParent()).getId() == R.id.unmatched_list) {
                ids.add(unmatchedList.get(linkName));
                main.Merge(name, ids.toArray(new String[ids.size()]), listItem);
                //matched list
            } else if (((View) p2.getParent()).getId() == R.id.matched_list) {
                Collections.addAll(ids, matchedList.get(linkName).split(":"));
                main.Merge(name, ids.toArray(new String[ids.size()]), listItem);
            }
        }
    }

    @Override
    public void onClick(View p1) {
        View unmatchedListView;
        View matchedListView;
        ViewGroup container = (ViewGroup) p1.getParent();
        TextView typeView;
        String type;

        switch (p1.getId()) {
            case R.id.delete_contact:
                HashSet<String> list = new HashSet<>();
                list.add(String.valueOf(id));
                ContactsHelper contacts = new ContactsHelper(main, listItem, name, list);
                contacts.deleteContacts();
                break;
            case R.id.unmatched_group:
                unmatchedListView = container.findViewById(R.id.unmatched_list_group);
                matchedListView = container.findViewById(R.id.matched_list_group);
                typeView = (TextView) container.findViewById(R.id.unmatched_group).findViewById(R.id.type);
                type = (String) typeView.getText();

                if (unmatchedList.size() != 0 && unmatchedListView.getVisibility() == View.GONE) {
                    matchedListView.setVisibility(View.GONE);
                    unmatchedListView.setVisibility(View.VISIBLE);
                    typeView.setText(type.replace('+', '-'));
                } else {
                    unmatchedListView.setVisibility(View.GONE);
                    matchedListView.setVisibility(View.GONE);
                    typeView.setText(type.replace('-', '+'));
                }
                typeView = (TextView) container.findViewById(R.id.matched_group).findViewById(R.id.type);
                type = (String) typeView.getText();
                typeView.setText(type.replace('-', '+'));
                break;
            case R.id.matched_group:
                unmatchedListView = container.findViewById(R.id.unmatched_list_group);
                matchedListView = container.findViewById(R.id.matched_list_group);
                typeView = (TextView) container.findViewById(R.id.matched_group).findViewById(R.id.type);
                type = (String) typeView.getText();

                if (matchedList.size() != 0 && matchedListView.getVisibility() == View.GONE) {

                    if (unmatchedListView.getVisibility() == View.GONE) {
                        matchedListView.setVisibility(View.VISIBLE);
                        unmatchedListView.setVisibility(View.GONE);
                    } else {
                        container.findViewById(R.id.matched_group).setTag(matchedListView);
                        unmatchedListView.setVisibility(View.GONE);
                    }

                    typeView.setText(type.replace('+', '-'));
                } else {
                    unmatchedListView.setVisibility(View.GONE);
                    matchedListView.setVisibility(View.GONE);
                    typeView.setText(type.replace('-', '+'));
                }
                typeView = (TextView) container.findViewById(R.id.unmatched_group).findViewById(R.id.type);
                type = (String) typeView.getText();
                typeView.setText(type.replace('-', '+'));
                break;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        main = (MainActivity) getActivity();

        Bundle args = getArguments();
        listItem = args.getString("listItem");
        name = args.getString("name");
        id = args.getString("ids");
        String accountSelected = listItem.split(":")[1];
        String accountOther = listItem.split(":")[2];

        //add trandotion handler to delay appear animation
        View view = inflater.inflate(R.layout.fragment_unmatched, container, false);
        final LayoutTransition transitioner = new LayoutTransition();
        transitioner.getAnimator(LayoutTransition.CHANGE_DISAPPEARING).addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                //Toast.makeText(main,anim.toString(),Toast.LENGTH_LONG).show();
                assert view != null;
                if (view.getTag() != null && view.getTag() instanceof View) {
                    //get current start delay
                    Long delay = transitioner.getStartDelay(LayoutTransition.APPEARING);
                    transitioner.setStartDelay(LayoutTransition.APPEARING, 0);
                    ((View) view.getTag()).setVisibility(View.VISIBLE);
                    view.setTag(null);
                    transitioner.setStartDelay(LayoutTransition.APPEARING, delay);
                }
            }
        });
        ((LinearLayout) view.findViewById(R.id.list)).setLayoutTransition(transitioner);
        Button btn = (Button) view.findViewById(R.id.delete_contact);
        btn.setOnClickListener(this);

        Comparator<String> caseInsensitive = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        };
        SharedPreferences pref = main.getSharedPreferences(Match.PREFKEY, Context.MODE_PRIVATE);
        HashSet<String> um = (HashSet<String>) pref.getStringSet(Match.UNMATCHNAMEKEY + accountOther + ":" + accountSelected, null);
        unmatchedList = new HashMap<>();
        int unmatchedCount = 0;
        ArrayList<String> unmatchedItems = new ArrayList<>();

        if (um != null) {
            for (String anUm : um) {
                String[] itemArray = (anUm).split(":");
                unmatchedList.put(itemArray[0], itemArray[1]);
                unmatchedItems.add(itemArray[0]);
            }
            Collections.sort(unmatchedItems, caseInsensitive);
            unmatchedCount = unmatchedItems.size();
        }

        //add heading
        View unmatchedLayout = view.findViewById(R.id.unmatched_group);
        TextView unmatchedType = (TextView) unmatchedLayout.findViewById(R.id.type);
        ListView unmatchedList = (ListView) view.findViewById(R.id.unmatched_list);
        unmatchedType.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.xxlarge_gap), LinearLayout.LayoutParams.WRAP_CONTENT));
        unmatchedType.setGravity(Gravity.CENTER);
        if (unmatchedCount == 0)
            unmatchedType.setText("  ");
        else
            unmatchedType.setText("+");
        ((TextView) unmatchedLayout.findViewById(R.id.value)).setText("Unmatched (" + unmatchedCount + ")");

        //add listview adapter
        AlphabetListAdapter unmatchedAdapter = new AlphabetListAdapter(
                main,
                view.findViewById(R.id.list),
                R.id.unmatched_list,
                R.id.unmatched_sideIndex,
                unmatchedItems);

        unmatchedList.setAdapter(unmatchedAdapter);

        //add listners
        unmatchedLayout.setOnClickListener(this);
        unmatchedList.setOnItemClickListener(this);

        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(main);
        HashSet<String> accountSet = (HashSet<String>) pref.getStringSet(Match.ACCOUNTKEY + accountSelected, null);
        HashMap<String, String> account = new HashMap<>();
        for (String entry : accountSet) {
            String contact[] = entry.split(":");
            account.put(contact[0], contact[1]);
        }

        HashSet<String> md = (HashSet<String>) pref.getStringSet(Match.MATCHEDKEY + accountSelected + ":" + accountOther, null);
        matchedList = new HashMap<>();
        ArrayList<String> matchedItems = new ArrayList<>();
        int matchedCount = 0;

        if (md != null) {
            for (String item : md) {
                String[] itemArray = (item).split(":");
                String itemName = account.get(itemArray[0]);
                matchedList.put(itemName, item);
                matchedItems.add(itemName);
            }
            Collections.sort(matchedItems, caseInsensitive);
            matchedCount = matchedItems.size();
        }

        //add heading
        View matchedLayout = view.findViewById(R.id.matched_group);
        TextView matchedType = (TextView) matchedLayout.findViewById(R.id.type);
        ListView matchedList = (ListView) view.findViewById(R.id.matched_list);
        matchedType.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.xxlarge_gap), LinearLayout.LayoutParams.WRAP_CONTENT));
        matchedType.setGravity(Gravity.CENTER);
        if (matchedCount == 0)
            matchedType.setText("  ");
        else
            matchedType.setText("+");
        ((TextView) matchedLayout.findViewById(R.id.value)).setText("Matched (" + matchedCount + ")");

        //add listview adapter
        AlphabetListAdapter matchedAdapter = new AlphabetListAdapter(
                main,
                view.findViewById(R.id.list),
                R.id.matched_list,
                R.id.matched_sideIndex,
                matchedItems);

        matchedList.setAdapter(matchedAdapter);

        //add listners
        matchedLayout.setOnClickListener(this);
        matchedList.setOnItemClickListener(this);

        return view;
    }
}
