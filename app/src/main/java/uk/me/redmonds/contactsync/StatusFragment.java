package uk.me.redmonds.contactsync;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatusFragment extends Fragment {
    private final static String LOG = "log";
    private final String logText;
    private OnViewCreatedListener mCallback;
    private TextView log;

    public StatusFragment() {
        logText = "";
    }

    public StatusFragment(String text) {
        logText = text;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // make sure activity has implemented callback interface
        try {
            mCallback = (OnViewCreatedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnViewCreatedListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //set actionbar title
        ((MainActivity) getActivity()).setHeading(getString(R.string.title_logs));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View statusView = inflater.inflate(R.layout.fragment_status, container, false);

        mCallback.onViewCreated(statusView);
        log = (TextView) statusView.findViewById(R.id.statuslog);
        log.setText(logText);

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            log.setText(savedInstanceState.getString(LOG, ""));
        }

        // Inflate the layout for this fragment
        return statusView;
    }

    public String getLog() {
        return log.getText().toString();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LOG, log.getText().toString());
        super.onSaveInstanceState(outState);
    }

    //Container Activity must implement this interface
    public interface OnViewCreatedListener {
        public void onViewCreated(View statusView);
    }
}
