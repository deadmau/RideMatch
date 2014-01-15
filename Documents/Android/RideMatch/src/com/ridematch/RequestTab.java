package com.ridematch;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

@SuppressLint("ValidFragment")
public class RequestTab extends Fragment {
	Context mContext;
	private ParseUser user;
	private JSONObject json;
	private String userChannel;
	private int day;
	
	public RequestTab(Context context) {
		mContext = context;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_request, null);
		user = ParseUser.getCurrentUser();
		json = user.getJSONObject("info");
        try {
			userChannel = json.getString("name");
		} catch (JSONException e) {
			e.printStackTrace();
		}
        Calendar c = Calendar.getInstance();
        day = c.get(Calendar.DAY_OF_WEEK);
        
        boolean requested = false;
        try {
			requested = json.getBoolean("RideRequest");
		} catch (JSONException e) {
			e.printStackTrace();
		}
        if(requested){
        	PushService.unsubscribe(getActivity(), "Offer");
        	PushService.subscribe(getActivity(), userChannel, MainFragmentActivity.class);
        } else{
        	PushService.unsubscribe(getActivity(), userChannel);
        	PushService.subscribe(getActivity(), "Offer", MainFragmentActivity.class);
        }
        
        if(day == 2){
        	try {
				json.put("RideRequest", false);
			} catch (JSONException e) {
				e.printStackTrace();
			}
        	PushService.unsubscribe(getActivity(), userChannel);
			PushService.subscribe(getActivity(), "Offer", MainFragmentActivity.class);
			user.put("info", json);
        	user.saveInBackground(new SaveCallback() {
				@Override
				public void done(ParseException arg0) {
					
				}
        	});
        }
        
        // Set up the handler for the cancel button click
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean request = false;
		        try {
					request = json.getBoolean("RideRequest");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(request){
					// Set up a progress dialog
			        final ProgressDialog cancel = new ProgressDialog(getActivity());
			        cancel.setTitle("Please wait.");
			        cancel.setMessage("Canceling Request.  Please wait.");
			        cancel.show();
					try {
						json.put("RideRequest", false);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					PushService.unsubscribe(getActivity(), userChannel);
					PushService.subscribe(getActivity(), "Offer", MainFragmentActivity.class);
					user.put("info", json);
					user.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException arg0) {
							cancel.dismiss();
							Toast.makeText(getActivity(), "Request cancelled", Toast.LENGTH_SHORT).show();
						}
		        	});
				} else{
					Toast.makeText(getActivity(), "No request to be cancelled", Toast.LENGTH_SHORT).show();
				}
			}
        });
        
        // Set up the handler for the send button click
        Button sendButton = (Button) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
        	  boolean request = false;
		        try {
					request = json.getBoolean("RideRequest");
				} catch (JSONException e) {
					e.printStackTrace();
				}
        	if(request){
        		Toast.makeText(getActivity(), "You already sent a request", Toast.LENGTH_SHORT).show();
        		return;
        	} else if(day == 1){
        		Toast.makeText(getActivity(), "Sorry... Request poll reopens tomorrow", Toast.LENGTH_SHORT).show();
        		return;
        	}
            // Create the builder where the new RideRequest is created
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Would you like to send a request?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
            	// Set up a progress dialog
                  final ProgressDialog save = new ProgressDialog(getActivity());
                  save.setTitle("Please wait.");
                  save.setMessage("Requesting.  Please wait.");
                  save.show();
                  try {
      				json.put("RideRequest", true);
      			  } catch (JSONException e) {
      				e.printStackTrace();
      			  }
            	  PushService.unsubscribe(getActivity(), "Offer");
				  PushService.subscribe(getActivity(), userChannel, MainFragmentActivity.class);
				  user.put("info", json);
            	  user.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException arg0) {
							save.dismiss();
							Toast.makeText(getActivity(), "Request sent", Toast.LENGTH_SHORT).show(); 
						}
            	  });
              }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                // Do nothing.
              }
            });
            alert.create().show();
          }
        });
		return view;
	}
}