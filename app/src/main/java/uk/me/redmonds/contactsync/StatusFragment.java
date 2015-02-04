package uk.me.redmonds.contactsync;

import android.app.ActionBar;
import android.app.Activity;
import android.os.*;
import android.app.Fragment;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

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

        /*MainActivity main = (MainActivity)this.getActivity();
        main.showMenuIcon();
        ActionBar actionBar = main.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.show();
        actionBar.setTitle(R.string.title_activity_main);*/

        mCallback.onViewCreated(statusView);

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            TextView log = (TextView)statusView.findViewById(R.id.statuslog);
            log.setText (savedInstanceState.getString("log", ""));
        }

        // Inflate the layout for this fragment
        return statusView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        TextView log = (TextView)getView().findViewById(R.id.statuslog);
        outState.putString("log", ((String)log.getText()));
        Toast.makeText(getActivity().getApplicationContext(), "log", Toast.LENGTH_LONG ).show();
    }
}
