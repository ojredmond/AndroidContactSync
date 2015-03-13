package uk.me.redmonds.contactsync;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.*;
import android.widget.*;

public class StatusFragment extends Fragment {
    //private final static String LOG = "log";
    private OnViewCreatedListener mCallback;
    private TextView log;
	private ScrollView scroll;

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

		//scroll.fullScroll(ScrollView.FOCUS_DOWN);
        //set actionbar title
        ((MainActivity) getActivity()).setHeading(getString(R.string.title_logs));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View statusView = inflater.inflate(R.layout.fragment_status, container, false);

        //Bundle args = getArguments();
        //String logText = args.getString("logText", "");

        mCallback.onViewCreated(this);
        log = (TextView) statusView.findViewById(R.id.statuslog);
		
		refresh();
		/*scroll = (ScrollView) statusView.findViewById(R.id.scroll);
		scroll.post(new Runnable () {
				public void run () {
					scroll.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});*/

        /*if (savedInstanceState != null) {
            // Restore last state for checked position.
            log.setText(savedInstanceState.getString(LOG, ""));
        }*/

        // Inflate the layout for this fragment
        return statusView;
    }

    public String getLog() {
        return log.getText().toString();
    }

	public void refresh () {
		SharedPreferences logPref = getActivity().getSharedPreferences(Match.LOG_TAG, Context.MODE_PRIVATE);
		String logText = logPref.getString(Match.LOG_TAG,"");
		log.setText(logText);
	}
	
    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LOG, log.getText().toString());
        super.onSaveInstanceState(outState);
    }*/

    //Container Activity must implement this interface
    public interface OnViewCreatedListener {
        public void onViewCreated(StatusFragment status);
    }
}
