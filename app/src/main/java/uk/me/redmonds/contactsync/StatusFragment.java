package uk.me.redmonds.contactsync;

import android.app.ActionBar;
import android.app.Activity;
import android.os.*;
import android.support.v4.app.Fragment;
import android.view.*;

public class StatusFragment extends Fragment {
    private OnViewCreatedListener mCallback;

    //Container Activity must implement this interface
    public interface OnViewCreatedListener {
        public void onViewCreated(View statusView);
    }

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);

        // make sure activity has implemented callback interface
        try {
            mCallback = (OnViewCreatedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException (activity.toString() + " must implement OnViewCreatedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View statusView = inflater.inflate(R.layout.status, container, false);

        MainActivity main = (MainActivity)this.getActivity();
        main.showMenuIcon();
        ActionBar actionBar = main.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.show();
        actionBar.setTitle(R.string.title_activity_main);

        mCallback.onViewCreated(statusView);

        // Inflate the layout for this fragment
        return statusView;
    }
}