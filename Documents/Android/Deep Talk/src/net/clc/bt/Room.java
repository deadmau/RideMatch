package net.clc.bt;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Room extends Activity {
	Button button1,button2,button3;
	private static final int SERVER_LIST_RESULT_CODE = 42;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room);
		addListenerOnButton();
	}
	
	public void addListenerOnButton() {

		button1 = (Button) findViewById(R.id.b1);
		button1.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View arg0) {
		Intent i = new Intent(Room.this, MakeRoom.class);
		startActivity(i); 


		}
});
		button2 = (Button) findViewById(R.id.b2);
		button2.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View arg0) {
            Intent clientIntent = new Intent(Room.this, Server.class);
            clientIntent.putExtra("TYPE", 1);
            startActivity(clientIntent);
            //Intent serverListIntent = new Intent(Room.this, Server.class);
            


		}
	

});
		button3 = (Button) findViewById(R.id.b3);
		button3.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View arg0) {
		Intent i = new Intent(Room.this, MyPage.class);
		startActivity(i); 


		}
	

});
		
	}}