package com.ridematch;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

@SuppressLint("ValidFragment")
public class InboxTab extends ListFragment implements LoaderManager.LoaderCallbacks<DataHandler> {

	Context mContext;
	// This is the Adapter being used to display the list's data.
	BaseAdapter mAdapter;
	public static ListView listView;
	public static FragmentActivity mainFragmentActivity;
	public static ArrayList<String> arr;
	
	public InboxTab(Context context) {
		mContext = context;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    mainFragmentActivity = getActivity();

	    // Create an empty adapter we will use to display the loaded data.
	    setListAdapter(mAdapter);

	    // Start out with a progress indicator.
	    setListShown(false);
	    listView = getListView();
	    
	    getActivity().getSupportLoaderManager().initLoader(10, null, this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public Loader<DataHandler> onCreateLoader(int arg0, Bundle arg1) {
		AsynChro asynChro = new AsynChro(InboxTab.this.getActivity());
		asynChro.forceLoad();
		return asynChro;
	}

	@Override
	public void onLoadFinished(Loader<DataHandler> arg0, DataHandler arg1) {
		listView.setAdapter(new DataBinder(getActivity(), arg1.getData()));
		setListShown(true);
		if(arr.get(0).toString() == "No match results"){
			return;
		}
		listView.setOnItemClickListener(new OnItemClickListener() {
	    	@Override
	        public void onItemClick(AdapterView<?> adapter, View view, int position, long arg)   {
	    		LayoutInflater inflater=LayoutInflater.from(getActivity());
	            View buttonView = inflater.inflate(R.layout.drawable_buttons, null);
	            ContextThemeWrapper cw = new ContextThemeWrapper(getActivity(), R.style.AlertDialogTheme);
	            AlertDialog.Builder builder=new AlertDialog.Builder(cw);
	            builder.setView(buttonView);
	            builder.setTitle("Choose");
	            
	            ImageButton callButton=(ImageButton) buttonView.findViewById(R.id.call);
	            int start = arr.get(position).indexOf("tel:");
    			int end = arr.get(position).length();
	            final String str = arr.get(position).substring(start, end);
	            if(str.contains("n/a")) {
	            	callButton.setVisibility(View.GONE);
	    			callButton.setClickable(false);
	    		}
	            callButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent callIntent = new Intent(Intent.ACTION_CALL);
			    		callIntent.setData(Uri.parse(str));
			    		startActivity(callIntent);
					}
	            });
	            
	            ImageButton naviButton=(ImageButton) buttonView.findViewById(R.id.navi);
	            int startAddr = arr.get(position).indexOf("Address:");
	            if(startAddr == -1){
	            	naviButton.setVisibility(View.GONE);
	    			naviButton.setClickable(false);
	            } else{
	            	int endAddr = arr.get(position).indexOf(", tel:");
		            final String addr = arr.get(position).substring(startAddr+8, endAddr);
		            naviButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent navigation = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=&daddr="+addr)); 
							startActivity(navigation);
							
						}
		            });
	            }
	            builder.create();
	            builder.show();
	        }
	    });
	}

	@Override
	public void onLoaderReset(Loader<DataHandler> arg0) {
		listView.setAdapter(null);
	}

	public static class AsynChro extends AsyncTaskLoader<DataHandler> {

		DataHandler mHaDataHandler;

		public AsynChro(Context context) {
			super(context);
			mHaDataHandler = new DataHandler();
		}

		@Override
		public DataHandler loadInBackground() {
			if(arr == null) {
				arr = new ArrayList<String>();
				arr.add("No match results");
			}
			try {
				synchronized (this) {
					wait(1000);
				}
			} catch (Exception e) {
				e.getMessage();
			}
			mHaDataHandler.setData(arr);
			return mHaDataHandler;
		}

		@Override
		public void deliverResult(DataHandler data) {
			super.deliverResult(data);
			listView.setAdapter(new DataBinder(mainFragmentActivity, data
					.getData()));
		}
	}
}