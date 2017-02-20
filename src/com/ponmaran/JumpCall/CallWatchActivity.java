package com.ponmaran.JumpCall;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallWatchActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_call_bridge);
//		getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent thisIntent = this.getIntent();

        String orgNum = thisIntent.getStringExtra(MainActivity.EXT_ORG_NUM);
        String numSeq = thisIntent.getStringExtra(MainActivity.EXT_BUILT_NUM_SEQ);

//		Toast.makeText(getApplicationContext(), "Calling " + orgNum + " using JumpCall", Toast.LENGTH_LONG).show();

        MainActivity.BRIDGE_PAIRS = MainActivity.BRIDGE_PAIRS + "~" + orgNum + "~" + PhoneNumberUtils.extractNetworkPortion(numSeq);

        if(((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getCallState() == TelephonyManager.CALL_STATE_OFFHOOK){
//        	System.out.println("Call in progress. Not starting listener");
        }
        else{
        	startService(new Intent(this, CallLogWatchService.class));
        	((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).listen(new EndCallListener(), PhoneStateListener.LISTEN_CALL_STATE);        	
        }
        this.finish();
    }

/*	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_call_bridge, menu);
        return true;
    }
*/
    private class EndCallListener extends PhoneStateListener{
    	
    	boolean activeCallInd = false;
//    	String curLogNum = new String();
    	
    	@Override
    	public void onCallStateChanged(int state, String incomingNumber) {

    		switch (state){
    		case TelephonyManager.CALL_STATE_OFFHOOK:
    			activeCallInd = true;
//    	        System.out.println("OffHook time:\t" + dte.toLocaleString() + "\t" + dte.getTime());
            	break;

    		case TelephonyManager.CALL_STATE_IDLE:
            	if (activeCallInd == true ){
        	        System.out.println("After Call Idle state time :: Date\t" + (new Date()).toString() + "\t" + new Date().getTime());
            		((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).listen(this, LISTEN_NONE); 

//This is to hold until the call log is updated
/*            		try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Sleep interrupted :@");
						e.printStackTrace();
					}

            		Cursor c = null;

            		String[] fields = {
        		    android.provider.CallLog.Calls.NUMBER,
        		    android.provider.CallLog.Calls.DURATION,
        		    android.provider.CallLog.Calls.DATE,
        		    };

            		c = getContentResolver().query(
            				android.provider.CallLog.Calls.CONTENT_URI,
            				fields,
		        		    android.provider.CallLog.Calls.DATE + " > " + (dte.getDate() - 1),
		        		    null,
		            		android.provider.CallLog.Calls.DATE + " DESC" 
		        		    );

	        		if(c.moveToFirst()){

	        			String[] callStack = MainActivity.BRIDGE_PAIRS.split("~");

	            		int callStackIdx =callStack.length - 1; 

	            		do{
		        			curLogNum = c.getString(c.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
		        			Long callDate = c.getLong(c.getColumnIndex(android.provider.CallLog.Calls.DATE));
		        			Long callDuration = c.getLong(c.getColumnIndex(android.provider.CallLog.Calls.DURATION));
//	        				System.out.println("Current log # : " + curLogNum);

		        			if (curLogNum.equals(callStack[callStackIdx])){
//		        				System.out.println("Bridge # from stack: " + callStack[callStackIdx]);

		        				ContentValues valueSet = new ContentValues();
		    	    	    	valueSet.put(android.provider.CallLog.Calls.NUMBER, callStack[callStackIdx - 1]);

//		    	    	    	int numRowsUpdt = getContentResolver().update(
				    	    	getContentResolver().update(
		    	    	    			android.provider.CallLog.Calls.CONTENT_URI, 
		    	    	    			valueSet, 
		    	    	    			android.provider.CallLog.Calls.NUMBER + "=" + curLogNum
		    	    	    			+ " and " + android.provider.CallLog.Calls.DATE + "=" + callDate
		    	    	    			+ " and " + android.provider.CallLog.Calls.DURATION + "=" + callDuration 
		    	    	    			,null);

//		    	    	    	System.out.println("Called # from stack: " + callStack[callStackIdx]);
//			        			System.out.println("Rows updated: " + numRowsUpdt);
			        			callStackIdx = callStackIdx - 2;
		        			}
//	        				System.out.println("Cursor @ " + curLogNum);
	        			}
	        			while (c.moveToNext() && callStackIdx > 0);
	        			MainActivity.BRIDGE_PAIRS = new String();
	        		}
*/            	}
            	else{
//            		System.out.println("Before call idle state :: Date:\t" + dte.toLocaleString());
            	};
    		}
        }
    }
    
/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/
}