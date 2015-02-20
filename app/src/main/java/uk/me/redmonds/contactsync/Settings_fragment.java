package uk.me.redmonds.contactsync;

import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.widget.Spinner;
import android.view.*;
import android.os.*;
import android.preference.*;
import android.content.SharedPreferences;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.ArrayList;


public class Settings_fragment extends Fragment
	implements OnItemSelectedListener,
			   OnCheckedChangeListener
{
	private SharedPreferences pref;
	private SharedPreferences.Editor prefEdit;
	private MainActivity main;

	@Override
	public void onCheckedChanged(CompoundButton p1, boolean isChecked)
	{
		if(prefEdit == null) prefEdit = pref.edit();
		
		if(p1.getId() == R.id.groups) {
			prefEdit.putBoolean(main.GROUPS, p1.isChecked());
		}
		
		if(p1.getId() == R.id.pictures) {
			prefEdit.putBoolean(main.PHOTOS, p1.isChecked());
		}
		
		if(p1.getId() == R.id.deep) {
			prefEdit.putBoolean(main.DEEP, p1.isChecked());
		}
		
		if(prefEdit != null) prefEdit.apply();
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		String accountName = (String)parent.getItemAtPosition(pos);
        String account1Name = pref.getString(main.ACCOUNT1, null);
        String account2Name = pref.getString(main.ACCOUNT2, null);
		if(prefEdit == null) prefEdit = pref.edit();

		if(((View)view.getParent()).getId() == R.id.account1selector
			&& !accountName.equals(account1Name))
			prefEdit.putString(main.ACCOUNT1, accountName);
		if(((View)view.getParent()).getId() == R.id.account2selector
		   && !accountName.equals(account2Name))
			prefEdit.putString(main.ACCOUNT2, accountName);
			
		if(prefEdit != null) prefEdit.apply();
	}
	
	public void onNothingSelected(AdapterView<?> parent) {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View preferenceView = inflater.inflate(R.layout.fragment_settings2, container, false);
		main = (MainActivity)getActivity();
		
		// Create an ArrayAdapter using the string array and a default spinner layout
        AccountManager accounts = AccountManager.get(this.getActivity());

        Account accountsList[] = accounts.getAccountsByType(getString(R.string.type));
        ArrayList<CharSequence> list = new ArrayList<>();

        for (Account a : accountsList) {
            list.add(a.name);
        }

        CharSequence[] listArray = list.toArray(new CharSequence[list.size()]);
		
		Spinner account1 = (Spinner)preferenceView.findViewById(R.id.account1selector);
		Spinner account2 = (Spinner)preferenceView.findViewById(R.id.account2selector);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter adapter = new ArrayAdapter(getActivity(),
												android.R.layout.simple_spinner_item,
												listArray);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		account1.setAdapter(adapter);
		account2.setAdapter(adapter);
		
		//get saved values
		pref = PreferenceManager.getDefaultSharedPreferences(main);
        String account1Name = pref.getString(main.ACCOUNT1, null);
        String account2Name = pref.getString(main.ACCOUNT2, null);
		
		//set default selected values
		if(account1Name == null) {
			if(listArray.length > 0) {
				prefEdit = pref.edit();
				prefEdit.putString(main.ACCOUNT1, (String)listArray[0]);
			}
		} else {
			account1.setSelection(list.indexOf(account1Name));
		}
		if(account2Name == null) {
			if(listArray.length > 0) {
				if(prefEdit == null) prefEdit = pref.edit();
				if(listArray.length > 1)
					prefEdit.putString(main.ACCOUNT2, (String)listArray[1]);
				else
					prefEdit.putString(main.ACCOUNT2, (String)listArray[0]);
			}
		} else {
			account2.setSelection(list.indexOf(account2Name));
		}
		
		if(prefEdit != null) prefEdit.apply();

		//set switches with saved state
		Boolean groupInc = pref.getBoolean(main.GROUPS, false);
		Boolean photoInc = pref.getBoolean(main.PHOTOS, false);
		Boolean deepInc = pref.getBoolean(main.DEEP, false);
		
		SwitchCompat groupSw = (SwitchCompat)preferenceView.findViewById(R.id.groups);
		groupSw.setChecked(groupInc);
		SwitchCompat photoSw = (SwitchCompat)preferenceView.findViewById(R.id.pictures);
		photoSw.setChecked(photoInc);
		SwitchCompat deepSw = (SwitchCompat)preferenceView.findViewById(R.id.deep);
		deepSw.setChecked(deepInc);
		
		// Add listeners
		account1.setOnItemSelectedListener(this);
		account2.setOnItemSelectedListener(this);
		groupSw.setOnCheckedChangeListener(this);
		photoSw.setOnCheckedChangeListener(this);
		deepSw.setOnCheckedChangeListener(this);
		
		//set actionbar title
        ((MainActivity)getActivity()).setHeading(getString(R.string.title_settings));
		
		return preferenceView;
	}
	
}
