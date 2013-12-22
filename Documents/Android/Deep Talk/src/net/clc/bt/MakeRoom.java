package net.clc.bt;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.model.OpenGraphAction;

public class MakeRoom extends Activity {
	private ListView listView;
	private ListView listView2;
    private List<BaseListElement> listElements;
    private List<BaseListElement> listElements2;
    private static final String PENDING_ANNOUNCE_KEY = "pendingAnnounce";
    private boolean pendingAnnounce;
	private static final String TAG = "MakeRoom";
	List<String> permissions;
	ProgressDialog dialog;
	Session.StatusCallback statusCallback = new SessionStatusCallback();
	Button button1,shareButton;
	EditText room, msg;
	String textmessage1, textmessage2, textmessage3, textmessage4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.make_room);
		addListenerOnButton();
		room = (EditText) findViewById(R.id.editText_text1);
        msg = (EditText) findViewById(R.id.editText_text3);
		shareButton = (Button) findViewById(R.id.publish);
		listView = (ListView) findViewById(R.id.selection_list);
		listView2 = (ListView) findViewById(R.id.selection_list2);
        /**
		 * Facebook Permissions
		 */
		
		permissions = new ArrayList<String>();
		permissions.add("publish_actions");
		
		/** End */

		/**initialize shareButton **/
		shareButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				publishStory();
			}
		});
		
		/**
		 * Facebook Session Initialization
		 */
		Session session = Session.getActiveSession();
		if(session == null) {
			if(savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
			}
			if(session == null) {
				session = new Session(this);
			}
			session.addCallback(statusCallback);
			Session.setActiveSession(session);
		}
		Log.d("FbShare", "Session State - " + session.getState());
		
		/** End **/
		
		/**initialize location window**/
		init(savedInstanceState);
	}
	
	private void init(Bundle savedInstanceState) {
		
        listElements = new ArrayList<BaseListElement>();
        listElements2 = new ArrayList<BaseListElement>();
        listElements.add(new LocationListElement(0));
        listElements2.add(new PeopleListElement(0));

        if (savedInstanceState != null) {
        	listElements.get(0).restoreState(savedInstanceState);
        	listElements2.get(0).restoreState(savedInstanceState);
            pendingAnnounce = savedInstanceState.getBoolean(PENDING_ANNOUNCE_KEY, false);
        }

        listView.setAdapter(new ActionListAdapter(MakeRoom.this, R.id.selection_list, listElements));
        listView2.setAdapter(new ActionListAdapter(MakeRoom.this, R.id.selection_list2, listElements2));
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            makeMeRequest(session);
        }
    }
	
	private void makeMeRequest(final Session session) {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                    }
                }
                if (response.getError() != null) {
                    //handleError(response.getError());
                }
            }
        });
        request.executeAsync();

    }
	
	public void addListenerOnButton() {

		button1 = (Button) findViewById(R.id.b1);
		button1.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View arg0) {
		Intent i = new Intent(MakeRoom.this, Server.class);
		startActivity(i); }});
	}
	
	/**
	 * Facebook Methods 
	 */
 
	private void publishStory() {
 
		Session session = Session.getActiveSession();
		if(session != null && session.getState().isOpened()) {
				checkSessionAndPost();
		} else {
			
			Log.d("FbShare", "Session is null");
			session = new Session(MakeRoom.this);
			Session.setActiveSession(session);
			session.addCallback(statusCallback);
			
			Log.d("FbShare", "Session info - " + session);
			try {
				Log.d("FbShare", "Opening session for read");
				session.openForRead(new Session.OpenRequest(MakeRoom.this));
			} catch(UnsupportedOperationException exception) {
				exception.printStackTrace();
				Log.d("FbShare", "Exception Caught");
				Toast.makeText(MakeRoom.this, "Unable to post your score on facebook", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void checkSessionAndPost (){
 
		Session session = Session.getActiveSession();
		session.addCallback(statusCallback);
		Log.d("FbShare", "Session Permissions Are - " + session.getPermissions());
			
		if(session.getPermissions().contains("publish_actions")) {
			publishAction(session);
		} else {
			session.requestNewPublishPermissions(new Session.NewPermissionsRequest(MakeRoom.this, permissions));
		} 
	}
	
	private void publishAction(Session session) {
 
		Log.d("FbShare", "Inside publishAction()");
		dialog = new ProgressDialog(MakeRoom.this);
		dialog.setMessage("Please wait...Posting the status");
		dialog.show();
		Bundle postParams = new Bundle();
		postParams.putString("name", "딥중");
		postParams.putString("place", ((LocationListElement) (listElements.get(0))).id);
		postParams.putString("tags", ((PeopleListElement) (listElements2.get(0))).friends);
		postParams.putString("message", msg.getText().toString());

		
		Request.Callback callback = new Request.Callback() {
 
			@Override
			public void onCompleted(Response response) {
				dialog.dismiss();
				FacebookRequestError error = response.getError();
				if(error != null) {
					Log.d("FbShare", "Facebook error - " + error.getErrorMessage());
					Log.d("FbShare", "Error code - " + error.getErrorCode());
					Log.d("FbShare", "JSON Response - " + error.getRequestResult());
					Log.d("FbShare", "Error Category - " + error.getCategory());
					Toast.makeText(MakeRoom.this, "Failed to share the post.Please try again", Toast.LENGTH_SHORT).show();
					shareButton.setEnabled(true);
				} else {
					Toast.makeText(MakeRoom.this, "Successfully shared the post", Toast.LENGTH_SHORT).show();
					shareButton.setEnabled(false);
				}
			}
		};
        
		Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);
 
		RequestAsyncTask asyncTask = new RequestAsyncTask(request);
		asyncTask.execute();
	}
	
	private class SessionStatusCallback implements Session.StatusCallback {
 
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			//Check if Session is Opened or not, if open & clicked on share button publish the story
			if(session != null && state.isOpened()) {
				Log.d("FbShare", "Session is opened");
				if(session.getPermissions().contains("publish_actions")) {
					Log.d("FbShare", "Starting share");
					publishAction(session);
				} else {
					Log.d("FbShare", "Session dont have permissions");
					publishStory();
				}
			} else {
				Log.d("FbShare", "Invalid fb Session");
			}
		}
 
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode >= 0 && requestCode < listElements.size()) {
            listElements.get(requestCode).onActivityResult(data);
            listElements2.get(requestCode).onActivityResult(data);
        }
		Log.d("FbShare", "Result Code is - " + resultCode +"");
		Session.getActiveSession().addCallback(statusCallback);
		Session.getActiveSession().onActivityResult(MakeRoom.this, requestCode, resultCode, data);
	}
 
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Save current session
		super.onSaveInstanceState(outState);
        listElements.get(0).onSaveInstanceState(outState);
        listElements2.get(0).onSaveInstanceState(outState);
        outState.putBoolean(PENDING_ANNOUNCE_KEY, pendingAnnounce);
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
	
	private void startPickerActivity(Uri data, int requestCode) {
        Intent intent = new Intent();
        intent.setData(data);
        intent.setClass(MakeRoom.this, PickerActivity.class);
        startActivityForResult(intent, requestCode);
    }
	
	private class LocationListElement extends BaseListElement {

        private static final String PLACE_KEY = "place";

        private GraphPlace selectedPlace = null;
        private LocationManager locManager = null;
        private boolean GPSOn = false;
        protected String id;
        
        public LocationListElement(int requestCode) {
            super(null, null, MakeRoom.this.getResources().getString(R.string.action_location_default), requestCode);
        }
        
        private boolean isGPSOn() {
        	locManager = (LocationManager) MakeRoom.this.getSystemService(Context.LOCATION_SERVICE);
            if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            	GPSOn = true;
                return GPSOn;
            } else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MakeRoom.this);
                alertDialogBuilder
                        .setMessage("GPS를 켜시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("예",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        Intent callGPSSettingIntent = new Intent(
                                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        MakeRoom.this.startActivity(callGPSSettingIntent);
                                        GPSOn = true;
                                    }
                                });
                alertDialogBuilder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                GPSOn = false;
                            }
                        });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
            return GPSOn;
        }

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                	boolean hasGPSOn;
                	if (Session.getActiveSession() != null && Session.getActiveSession().isOpened() && isGPSOn()) {
                		hasGPSOn = locManager.isProviderEnabled (LocationManager.GPS_PROVIDER);
                		if(hasGPSOn){
                			startPickerActivity(PickerActivity.PLACE_PICKER, getRequestCode());
                		}
                	}
                }
            };
        }

        @Override
        protected void onActivityResult(Intent data) {
            selectedPlace = ((MainApplication) MakeRoom.this.getApplication()).getSelectedPlace();
            setPlaceText();
            notifyDataChanged();
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
            if (selectedPlace != null) {
                action.setPlace(selectedPlace);
            }
        }

        @Override
        protected void onSaveInstanceState(Bundle bundle) {
            if (selectedPlace != null) {
                bundle.putString(PLACE_KEY, selectedPlace.getInnerJSONObject().toString());
            }
        }

        @Override
        protected boolean restoreState(Bundle savedState) {
            String place = savedState.getString(PLACE_KEY);
            if (place != null) {
                try {
                    selectedPlace = GraphObject.Factory
                            .create(new JSONObject(place), GraphPlace.class);
                    setPlaceText();
                    return true;
                } catch (JSONException e) {
                    Log.e(TAG, "Unable to deserialize place.", e);
                }
            }
            return false;
        }

        private void setPlaceText() {
            String text = "";
            if (selectedPlace != null) {
                text = selectedPlace.getName();
                id = selectedPlace.getId();
            }
            if (text == null) {
            	text = getResources().getString(R.string.action_location_default);
                id = "352925394727436"; 
            }
            setText2(text);
        }

    }
	
	private class PeopleListElement extends BaseListElement {
    	
    	private static final String FRIENDS_KEY = "friends";
    	
    	private List<GraphUser> selectedUsers;
    	private String friends = "";
    	
        public PeopleListElement(int requestCode) {
            super(null, null, MakeRoom.this.getResources().getString(R.string.action_people_default), requestCode);
        }
        
        @Override
        protected void onActivityResult(Intent data) {
            selectedUsers = ((MainApplication) MakeRoom.this
                     .getApplication())
                     .getSelectedUsers();
            setUsersText();
            notifyDataChanged();
        }
        
        @Override
        protected void onSaveInstanceState(Bundle bundle) {
            if (selectedUsers != null) {
                bundle.putByteArray(FRIENDS_KEY,
                        getByteArray(selectedUsers));
            }   
        } 
        
        @Override
        protected boolean restoreState(Bundle savedState) {
            byte[] bytes = savedState.getByteArray(FRIENDS_KEY);
            if (bytes != null) {
                selectedUsers = restoreByteArray(bytes);
                setUsersText();
                return true;
            }   
            return false;
        } 

        @Override
        protected View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                	startPickerActivity(PickerActivity.FRIEND_PICKER, getRequestCode());
                }
            };
        }
        
        private void setUsersText() {
            String text = "";
            if (selectedUsers != null) {
                    // If there is one friend
                if (selectedUsers.size() == 1) {
                    text = selectedUsers.get(0).getName();
                    friends = selectedUsers.get(0).getId();
                } else if (selectedUsers.size() == 2) {
                    // If there are two friends 
                    text = selectedUsers.get(0).getName()+","+selectedUsers.get(1).getName();
                    friends = selectedUsers.get(0).getId()+","+selectedUsers.get(1).getId();
                } else if (selectedUsers.size() > 2) {
                    // If there are more than two friends
                	int len = selectedUsers.size()-1;
                	for(int i = 0; i < len; i++){
                		text += selectedUsers.get(i).getName()+",";
                		friends += selectedUsers.get(i).getId()+",";
                	}
                	text += selectedUsers.get(len).getName();
                	friends += selectedUsers.get(len).getId();
                }   
            }   
            if (text == null) {
                // If no text, use the placeholder text
                text = getResources()
                .getString(R.string.action_people_default);
            }   
            // Set the text in list element. This will notify the 
            // adapter that the data has changed to
            // refresh the list view.
            setText2(text);
        } 
        
        private byte[] getByteArray(List<GraphUser> users) {
            // convert the list of GraphUsers to a list of String 
            // where each element is the JSON representation of the 
            // GraphUser so it can be stored in a Bundle
            List<String> usersAsString = new ArrayList<String>(users.size());

            for (GraphUser user : users) {
                usersAsString.add(user.getInnerJSONObject().toString());
            }   
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                new ObjectOutputStream(outputStream).writeObject(usersAsString);
                return outputStream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, "Unable to serialize users.", e); 
            }   
            return null;
        }
        
        private List<GraphUser> restoreByteArray(byte[] bytes) {
            try {
                @SuppressWarnings("unchecked")
                List<String> usersAsString =
                        (List<String>) (new ObjectInputStream
                                        (new ByteArrayInputStream(bytes)))
                                        .readObject();
                if (usersAsString != null) {
                    List<GraphUser> users = new ArrayList<GraphUser>
                    (usersAsString.size());
                    for (String user : usersAsString) {
                        GraphUser graphUser = GraphObject.Factory
                        .create(new JSONObject(user), 
                                        GraphUser.class);
                        users.add(graphUser);
                    }   
                    return users;
                }   
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Unable to deserialize users.", e); 
            } catch (IOException e) {
                Log.e(TAG, "Unable to deserialize users.", e); 
            } catch (JSONException e) {
                Log.e(TAG, "Unable to deserialize users.", e); 
            }   
            return null;
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
            if (selectedUsers != null) {
                action.setTags(selectedUsers);
            }   
        }  
    }
	
	private class ActionListAdapter extends ArrayAdapter<BaseListElement> {
	    private List<BaseListElement> listElements;

	    public ActionListAdapter(Context context, int resourceId, List<BaseListElement> listElements) {
	        super(context, resourceId, listElements);
	        this.listElements = listElements;
	        for (int i = 0; i < listElements.size(); i++) {
	            listElements.get(i).setAdapter(this);
	        }
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        if (view == null) {
	            LayoutInflater inflater =
	                    (LayoutInflater) MakeRoom.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            view = inflater.inflate(R.layout.listitem, null);
	        }

	        BaseListElement listElement = listElements.get(position);
	        if (listElement != null) {
	            view.setOnClickListener(listElement.getOnClickListener());
	            ImageView icon = (ImageView) view.findViewById(R.id.icon);
	            TextView text1 = (TextView) view.findViewById(R.id.text1);
	            TextView text2 = (TextView) view.findViewById(R.id.text2);
	            if (icon != null) {
	                icon.setImageDrawable(listElement.getIcon());
	            }
	            if (text1 != null) {
	                text1.setText(listElement.getText1());
	            }
	            if (text2 != null) {
	                text2.setText(listElement.getText2());
	            }
	        }
	        return view;
	    }

	}
}
	        
	        
	        
	       
      