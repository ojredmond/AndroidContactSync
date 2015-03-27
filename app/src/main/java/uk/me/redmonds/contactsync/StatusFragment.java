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
import java.util.*;

public class StatusFragment extends Fragment {
	public static final String LOG_TAG = "MATCH_STATUS";
    //private final static String LOG = "log";
    private OnViewCreatedListener mCallback;
    private TextView log;
    private ProgressBar progress;
    private TextView progressText;

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

        mCallback.onViewCreated(this);
        log = (TextView) statusView.findViewById(R.id.statuslog);
        progressText = (TextView) statusView.findViewById(R.id.progresstext);
        progress = (ProgressBar) statusView.findViewById(R.id.progressbar);
        refresh();

        // Inflate the layout for this fragment
        return statusView;
    }

    public String getLog() {
        return log.getText().toString();
    }

    public void refresh () {
        SharedPreferences logMatchPref = getActivity().getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);
        String logText = "";
        TreeMap<String,Object> map = new TreeMap<String,Object>();
        map.putAll(logMatchPref.getAll());
		
        Map<String,Object> mapR = map.descendingMap();
        for(Map.Entry <String,?> item: mapR.entrySet())
            if(item.getKey().startsWith(LOG_TAG))
                logText += item.getValue() + "\n";
        
        log.setText(logText);
        
        //update progress bar
        int prog = logMatchPref.getInt("PROGRESS",-1);
        int max = logMatchPref.getInt("MAX",0);
        String label = logMatchPref.getString("ACCOUNT", "");
        if(prog == -1) {
            progress.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
        } else {
            progress.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            progress.setProgress(prog);
            progress.setMax(max);
            progressText.setText(label);
        }
            
    }

    //Container Activity must implement this interface
    public interface OnViewCreatedListener {
        public void onViewCreated(StatusFragment status);
    }
}
