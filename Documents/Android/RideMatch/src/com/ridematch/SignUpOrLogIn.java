package com.ridematch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SignUpOrLogIn extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up_or_login);

    // Log in button click handler
    ((Button) findViewById(R.id.logInButton)).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        // Starts an intent of the log in activity
        startActivity(new Intent(SignUpOrLogIn.this, LogIn.class));
      }
    });

    // Sign up button click handler
    ((Button) findViewById(R.id.signUpButton)).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        // Starts an intent for the sign up activity
        startActivity(new Intent(SignUpOrLogIn.this, SignUp.class));
      }
    });
  }
}

