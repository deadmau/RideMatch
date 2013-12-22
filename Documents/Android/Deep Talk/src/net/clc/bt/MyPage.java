package net.clc.bt;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MyPage extends Activity {
	Button button1,button2,button3,button4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mypage);
		addListenerOnButton();
	}
	
	public void addListenerOnButton() {

		button1 = (Button) findViewById(R.id.b1);
		button1.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View arg0) {
		Intent i = new Intent(MyPage.this, Setting.class);
		startActivity(i); 


		}
});
		button2 = (Button) findViewById(R.id.b2);
		button2.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View arg0) {
		Intent i = new Intent(MyPage.this, Coupon.class);
		startActivity(i); 


		}
	

});
		button3 = (Button) findViewById(R.id.b3);
		button3.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View arg0) {
		Intent i = new Intent(MyPage.this, History.class);
		startActivity(i); 


		}
	

});
		button4 = (Button) findViewById(R.id.b4);
		button4.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View arg0) {
		Intent i = new Intent(MyPage.this, Rank.class);
		startActivity(i); 


		}
	

});
		
	}}