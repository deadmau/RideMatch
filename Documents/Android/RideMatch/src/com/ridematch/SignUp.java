package com.ridematch;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Activity which displays a login screen to the user.
 */
public class SignUp extends Activity {
  // UI references.
  private EditText usernameView;
  private EditText realnameView;
  private EditText emailView;
  private EditText phoneView;
  private EditText passwordView;
  private EditText passwordAgainView;
  private EditText capacityView;
  private RadioGroup carGroup;
  private boolean choice = false;
  private int capacity;
  protected static TextView addressView;
  protected static ParseGeoPoint point;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_signup);

    // Set up the signup form.
    usernameView = (EditText) findViewById(R.id.username);
    passwordView = (EditText) findViewById(R.id.password);
    realnameView = (EditText) findViewById(R.id.realname);
    emailView = (EditText) findViewById(R.id.email);
    phoneView = (EditText) findViewById(R.id.phone);
    passwordAgainView = (EditText) findViewById(R.id.passwordAgain);
    addressView = (TextView) findViewById(R.id.address);
    carGroup = (RadioGroup) findViewById(R.id.carGroup);
    capacityView = (EditText) findViewById(R.id.capacity);
    carGroup.check(R.id.no);
    capacityView.setVisibility(View.GONE);
    
    // Find address button click handler
    addressView.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
    	  Intent intent = new Intent(SignUp.this, MapFragmentActivity.class);
    	  startActivity(intent);
      	}
      });
    
    // Set up the selection handler to save the selection 
    carGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      public void onCheckedChanged(RadioGroup group, int checkedId) {
    	  if(checkedId == R.id.yes){
    		  choice = true;
    		  capacityView.setVisibility(View.VISIBLE);
    	  } else{
    		  choice = false;
    		  capacityView.setVisibility(View.GONE);
    	  }
      }
    });
    
    // Set up the submit button click handler
    findViewById(R.id.action_button).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        // Validate the sign up data
        boolean validationError = false;
        StringBuilder validationErrorMessage =
            new StringBuilder(getResources().getString(R.string.error_intro));
        if (isEmpty(usernameView)) {
          validationError = true;
          validationErrorMessage.append(getResources().getString(R.string.error_blank_username));
        }
        if (isEmpty(realnameView)) {
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.error_blank_realname));
          }
        if (isEmpty(emailView)) {
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.error_blank_email));
          }
        if (isEmpty(addressView)) {
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.error_blank_address));
          }
        if (isEmpty(passwordView)) {
          if (validationError) {
            validationErrorMessage.append(getResources().getString(R.string.error_join));
          }
          validationError = true;
          validationErrorMessage.append(getResources().getString(R.string.error_blank_password));
        }
        if (!isMatching(passwordView, passwordAgainView)) {
          if (validationError) {
            validationErrorMessage.append(getResources().getString(R.string.error_join));
          }
          validationError = true;
          validationErrorMessage.append(getResources().getString(
              R.string.error_mismatched_passwords));
        }
        if(capacityView.getVisibility() == View.VISIBLE){
        	try{
        		capacity = Integer.parseInt(capacityView.getText().toString());
        	} catch(Exception e){
        		validationError = true;
        		validationErrorMessage.append(getResources().getString(R.string.error_blank_capacity));
        	}
        }
        validationErrorMessage.append(getResources().getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
          Toast.makeText(SignUp.this, validationErrorMessage.toString(), Toast.LENGTH_LONG)
              .show();
          return;
        }

        // Set up a progress dialog
        final ProgressDialog dlg = new ProgressDialog(SignUp.this);
        dlg.setTitle("Please wait.");
        dlg.setMessage("Signing up.  Please wait.");
        dlg.show();
        
        JSONObject json = new JSONObject();
        // Set up a new Parse user
        ParseUser user = new ParseUser();
    	String phone_num = phoneView.getText().toString();
    	if(phone_num.contentEquals("")) {
    		try {
				json.put("phone", "n/a");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
    	} else{
    		try {
				json.put("phone", phone_num);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
    	}
    	
    	try {
    		json.put("name", realnameView.getText().toString());
            json.put("location", point);
            json.put("address", addressView.getText().toString());
            json.put("carOwner", choice);
            json.put("capacity", capacity);
            json.put("RideRequest", false);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
        user.setUsername(usernameView.getText().toString().replaceAll("\\s",""));
        user.setEmail(emailView.getText().toString());
        user.setPassword(passwordView.getText().toString());
        user.put("info", json);
        // Call the Parse signup method
        user.signUpInBackground(new SignUpCallback() {

          @Override
          public void done(ParseException e) {
            dlg.dismiss();
            if (e != null) {
              // Show the error message
              Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_LONG).show();
            } else {
              // Start an intent for the request activity
              Intent intent = new Intent(SignUp.this, StartActivity.class);
              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
            }
          }
        });
      }
    });
  }

  private boolean isEmpty(EditText etText) {
    if (etText.getText().toString().trim().length() > 0) {
      return false;
    } else {
      return true;
    }
  }
  
  private boolean isEmpty(TextView etText) {
	    if (etText.getText().toString().trim().length() > 0) {
	      return false;
	    } else {
	      return true;
	    }
	  }

  private boolean isMatching(EditText etText1, EditText etText2) {
    if (etText1.getText().toString().equals(etText2.getText().toString())) {
      return true;
    } else {
      return false;
    }
  }

}
