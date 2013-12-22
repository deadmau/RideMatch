package net.clc.bt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;


public class MainActivity extends Activity {
	private static List<String> permissions;
	Session.StatusCallback statusCallback = new SessionStatusCallback();
	ProgressDialog dialog;
	private Button button1, button2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		button1 = (Button) findViewById(R.id.b1);
		button1.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent i = new Intent(MainActivity.this, Room.class);
			startActivity(i); 
		}});
		
		button2 = (Button) findViewById(R.id.b2);
        /***** FB Permissions *****/
		
		permissions = new ArrayList<String>();
		permissions.add("email");
		
		/***** End FB Permissions *****/
		
		button2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Check if there is any Active Session, otherwise Open New Session
				Session session = Session.getActiveSession();
				
				if(!session.isOpened()) {
					session.openForRead(new Session.OpenRequest(MainActivity.this).setCallback(statusCallback).setPermissions(permissions));
				} else {
					Session.openActiveSession(MainActivity.this, true, statusCallback);
				}
			}
		});
		
		Session session = Session.getActiveSession();
		if(session == null) {
			if(savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
			}
			if(session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			session.addCallback(statusCallback);
			if(session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback).setPermissions(permissions));
			} 
		}
	}
	
	
	private class SessionStatusCallback implements Session.StatusCallback {
 
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			//Check if Session is Opened or not
			processSessionStatus(session, state, exception);
		}
		
	}
 
	public void processSessionStatus(Session session, SessionState state, Exception exception) {
		
		if(session != null && session.isOpened()) {
			
			if(session.getPermissions().contains("email")) {
				//Show Progress Dialog 
				dialog = new ProgressDialog(MainActivity.this);
				dialog.setMessage("로그인 중...");
				dialog.show();
				Request.newMeRequest(session, new Request.GraphUserCallback() {
 
					@Override
					public void onCompleted(GraphUser user, Response response) {
						
 
						if (dialog!=null && dialog.isShowing()) {
							dialog.dismiss();
						}
						if(user != null) {
							Map<String, Object> responseMap = new HashMap<String, Object>();
							GraphObject graphObject = response.getGraphObject();
							responseMap = graphObject.asMap();
							Log.i("FbLogin", "Response Map KeySet - " + responseMap.keySet());
							// TODO : Get Email responseMap.get("email"); 
							
							String fb_id = user.getId();
							String email = null;
							String name = (String) responseMap.get("name");
							if (responseMap.get("email")!=null) {
								email = responseMap.get("email").toString();
								//TODO Login successfull Start your next activity
								startActivity(new Intent(MainActivity.this, Room.class));
							}
							else {
								//Clear all session info & ask user to login again
								Session session = Session.getActiveSession();
								if(session != null) {
									session.closeAndClearTokenInformation();
								}
							}
						}
					}
				}).executeAsync();
			} else {
				session.requestNewReadPermissions(new Session.NewPermissionsRequest(MainActivity.this, permissions));
			}
		}
	}
		
	/********** Activity Methods **********/
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("FbLogin", "Result Code is - " + resultCode +"");
		Session.getActiveSession().onActivityResult(MainActivity.this, requestCode, resultCode, data);
	}
 
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Save current session
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}
	
	@Override
	protected void onStart() {
		// TODO Add status callback
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}
	
	@Override
	protected void onStop() {
		// TODO Remove callback
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}
}