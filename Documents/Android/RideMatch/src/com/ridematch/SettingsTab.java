package com.ridematch;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@SuppressLint("ValidFragment")
public class SettingsTab extends Fragment {

  Context mContext;
  private RadioGroup carOwnerGroup;
  private EditText capacityView;
  private TextView addressView;
  private ParseUser user;
  private JSONObject json;
  private boolean hasCar;
  private boolean choice;
  private int capacity;
	
  public SettingsTab(Context context) {
	mContext = context;
  }
	
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.activity_setting, null);
	user = ParseUser.getCurrentUser();
	json = user.getJSONObject("info");
    addressView = (TextView) view.findViewById(R.id.changeAddress);
    try {
		addressView.setText(json.getString("address"));
		hasCar = json.getBoolean("carOwner");
		capacity = json.getInt("capacity");
	} catch (JSONException e1) {
		e1.printStackTrace();
	}
    carOwnerGroup = (RadioGroup) view.findViewById(R.id.carOwnerGroup);
    capacityView = (EditText) view.findViewById(R.id.capacity);
    
    if(hasCar){
    	carOwnerGroup.check(R.id.yesButton);
    	capacityView.setText(""+capacity);
        capacityView.setVisibility(View.VISIBLE);
        choice = true;
    } else{
    	carOwnerGroup.check(R.id.noButton);
    	capacityView.setVisibility(View.GONE);
    	choice = false;
    }
    
    // Set up the selection handler to save the selection 
    carOwnerGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      public void onCheckedChanged(RadioGroup group, int checkedId) {
    	  if(checkedId == R.id.yesButton){
    		  capacityView.setVisibility(View.VISIBLE);
    		  choice = true;
    	  } else{
    		  capacityView.setVisibility(View.GONE);
    		  capacityView.setText(null);
    		  choice = false;
    	  }
      }
    });
    
    // Set up the change address click handler
    addressView.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        // Start and intent for the google map activity
        Intent intent = new Intent(getActivity(), MapFragmentActivity.class);
        startActivity(intent);
      }
    });
    
    // Set up the save button click handler
    ((Button) view.findViewById(R.id.save)).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
    	  // Set up a progress dialog
          final ProgressDialog save = new ProgressDialog(getActivity());
          save.setTitle("Please wait.");
          save.setMessage("Saving.  Please wait.");
          save.show();
    	  try {
			json.put("carOwner", choice);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
    	  if(choice){
    		  try{
    			  json.put("capacity", Integer.parseInt(capacityView.getText().toString()));
    		  } catch(Exception e){
    			  Toast.makeText(getActivity(), R.string.error_blank_capacity, Toast.LENGTH_SHORT).show();
    		  }
    	  }
    	  user.put("info", json);
    	  user.saveInBackground(new SaveCallback() {
              @Override
              public void done(ParseException e) {
            	  save.dismiss();
            	  Toast.makeText(getActivity(), "Change saved", Toast.LENGTH_SHORT).show();
              }
    	  });
      }
    });
 

    // Set up the log out button click handler
    ((Button) view.findViewById(R.id.logOutButton)).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        // Call the Parse log out method
        ParseUser.logOut();
        // Start and intent for the dispatch activity
        Intent intent = new Intent(getActivity(), StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
      }
    });
    
	return view;
  }
}
