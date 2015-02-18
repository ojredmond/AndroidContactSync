package uk.me.redmonds.contactsync;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatusFragment extends Fragment {
    private OnViewCreatedListener mCallback;
    private TextView log;

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

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            log.setText(savedInstanceState.getString("log", ""));
        }

        // Inflate the layout for this fragment
        return statusView;
    }

    //Container Activity must implement this interface
    public interface OnViewCreatedListener {
        public void onViewCreated(View statusView);
    }
}
