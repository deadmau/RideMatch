package com.ridematch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class RequestUserInfo extends Activity{
	  private EditText emailView;
	  
	 @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_reset);
	    
	    emailView = (EditText) findViewById(R.id.emailAddress);

	    // Set up the reset button click handler
	    ((Button) findViewById(R.id.reset_button)).setOnClickListener(new OnClickListener() {
	      public void onClick(View v) {
	    	  ParseUser.requestPasswordResetInBackground(emailView.getText().toString(),
                      new RequestPasswordResetCallback() {
	    		  public void done(ParseException e) {
	    			  if (e == null) {
	    				  Toast.makeText(RequestUserInfo.this, "email sent", Toast.LENGTH_SHORT).show();
	    			  } else {
	    				  Toast.makeText(RequestUserInfo.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
	    			  }
	    		  }
	    	  });
	      }
	    });
	 }
}
