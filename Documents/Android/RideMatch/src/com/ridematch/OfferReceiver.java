package com.ridematch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OfferReceiver extends BroadcastReceiver {
	
	public static boolean matchOffer;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent in = new Intent("ko.ridematch.MAIN");
		context.sendBroadcast(in);	
		matchOffer = true;
	}
}
