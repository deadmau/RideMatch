package net.clc.bt;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FindRoom extends Activity {
	Button button1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.findroom);
		addListenerOnButton();
	}
	
	public void addListenerOnButton() {

		button1 = (Button) findViewById(R.id.b1);
		button1.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View arg0) {
		Intent i = new Intent(FindRoom.this, GuestStart.class);
		startActivity(i); 


		}
	

});}}