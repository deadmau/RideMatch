package com.ridematch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ResultReceiver extends BroadcastReceiver {
	
	public static boolean matchResult;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
        if (extras == null) {
        	return;
        }
        Intent in = new Intent("com.ridematch.UPDATE_STATUS").putExtras(extras);
		context.sendBroadcast(in);
		matchResult = true;
	}
}